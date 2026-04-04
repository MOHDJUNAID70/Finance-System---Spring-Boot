package zorvyn.assessment.Idempotency;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class IdempotencyKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String requestHash;

    @Lob
    private String body;

    private LocalDateTime createdAt=LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
