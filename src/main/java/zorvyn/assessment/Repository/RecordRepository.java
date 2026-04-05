package zorvyn.assessment.Repository;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zorvyn.assessment.Enum.RecordType;
import zorvyn.assessment.Model.Record;
import zorvyn.assessment.Model.Users;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<Record, Integer>, JpaSpecificationExecutor<Record> {
    List<Record> findByCreatedBy(Users user);

    @Query("SELECT SUM(f.amount) FROM Record f WHERE f.type = :type AND f.deleted = false")
    BigDecimal sumByType(@Param("type") RecordType recordType);

    @Query("SELECT f.category, SUM(f.amount) FROM Record f WHERE f.deleted = false GROUP BY f.category")
    List<Object[]> sumByCategory();

    @Query("SELECT EXTRACT(MONTH FROM f.date), SUM(f.amount), f.type " +
            "FROM Record f " +
            "WHERE EXTRACT(YEAR FROM f.date) = :year " +
            "AND f.deleted = false " +
            "GROUP BY EXTRACT(MONTH FROM f.date), f.type " +
            "ORDER BY EXTRACT(MONTH FROM f.date)")
    List<Object[]> monthlyTrends(@Param("year") int year);

    List<Record> findTop5ByDeletedFalseOrderByCreatedAtDesc();

    Record findByAmountAndTypeAndCategoryAndDateAndCreatedByAndDeletedFalse(@NotNull(message = "Amount is required")
                                                             @DecimalMin(value = "0.01", message = "Amount must be greater than 0") BigDecimal amount,
                                                             @NotNull(message = "Type is required") RecordType type,
                                                             @NotNull(message = "Category is required") String category,
                                                             @NotNull(message = "Date is required") LocalDate date, Users user);

    @Query("SELECT SUM(f.amount) FROM Record f " +
            "WHERE f.type = :type " +
            "AND f.createdBy = :user " +
            "AND f.deleted = false")
    BigDecimal sumByTypeAndCreatedBy(@Param("type") RecordType recordType, @Param("user") Users user);

    List<Record> findTop5ByCreatedByAndDeletedFalseOrderByCreatedAtDesc(Users user);
}
