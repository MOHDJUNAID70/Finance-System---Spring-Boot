package zorvyn.assessment.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor

public class RateLimitFilter extends OncePerRequestFilter {

    // this bucket for 5 requests/minute
    private final Map<String, Bucket> authBucket= new ConcurrentHashMap<>();

    // this bucket for 20 requests/minute
    private final Map<String, Bucket> generalBucket= new ConcurrentHashMap<>();

    public Bucket createAuthBucket(){
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5,
                        Refill.intervally(5, Duration.ofMinutes(1))))
                .build();
    }

    public Bucket createGeneralBucket(){
        return Bucket.builder()
                .addLimit(Bandwidth.classic(20,
                        Refill.greedy(20, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            String ip=request.getHeader("X-Forwarded-For") != null
                    ? request.getHeader("X-Forwarded-For").split(",")[0].trim()
                    : request.getRemoteAddr();
            String path = request.getRequestURI();

            Bucket bucket;
            if(path.startsWith("/api/auth/")){
                bucket=authBucket.computeIfAbsent(ip, k -> createAuthBucket());
            }
            else{
                bucket=generalBucket.computeIfAbsent(ip, k -> createGeneralBucket());
            }

        ConsumptionProbe prob=bucket.tryConsumeAndReturnRemaining(1);
        if(prob.isConsumed()){
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(prob.getRemainingTokens()));
            filterChain.doFilter(request, response);
        }
        else{
            long doWait=prob.getNanosToWaitForRefill()/1_000_000_000;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(doWait));
            response.getWriter().write("{\"error\": \"Too many requests. Please try again in " + doWait + " seconds.\"}");
        }
    }
}
