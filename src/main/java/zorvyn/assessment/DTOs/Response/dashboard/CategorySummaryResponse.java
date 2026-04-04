package zorvyn.assessment.DTOs.Response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorySummaryResponse {
    private String category;
    private BigDecimal total;
}
