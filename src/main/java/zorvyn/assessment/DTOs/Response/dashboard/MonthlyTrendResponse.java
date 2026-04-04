package zorvyn.assessment.DTOs.Response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zorvyn.assessment.Enum.RecordType;
import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyTrendResponse {
    private int month;
    private BigDecimal amount;
    private RecordType type;
}
