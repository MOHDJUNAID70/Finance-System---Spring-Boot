package zorvyn.assessment.Idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, Integer> {

    IdempotencyKey findByIdempotencyKeyAndExpiresAtAfter(String key, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM IdempotencyKey i WHERE i.expiresAt < :now")
    void deleteByExpiresAtBefore(@RequestParam("now") LocalDateTime now);
}
