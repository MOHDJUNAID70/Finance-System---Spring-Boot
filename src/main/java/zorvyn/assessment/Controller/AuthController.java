package zorvyn.assessment.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zorvyn.assessment.DTOs.Request.LoginUserRequest;
import zorvyn.assessment.DTOs.Request.RegisterUserRequest;
import zorvyn.assessment.DTOs.Response.AuthResponse;
import zorvyn.assessment.Service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication APIs", description = "These APIs are secured with RateLimiting. User can only make 5 requests per minute to these endpoints. Exceeding this limit will result in a 429 Too Many Requests response.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterUserRequest request) {
        authService.register(request);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT Token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginUserRequest request) {
        return ResponseEntity.ok(authService.verify(request));
    }
}
