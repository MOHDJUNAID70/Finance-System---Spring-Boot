package zorvyn.assessment.Idempotency;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@EnableScheduling
public class IdempotencyCleanUp {

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanup() {
        idempotencyRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
