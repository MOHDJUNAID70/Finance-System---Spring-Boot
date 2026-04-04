package zorvyn.assessment.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zorvyn.assessment.DTOs.Response.RecordResponse;
import zorvyn.assessment.DTOs.Response.dashboard.CategorySummaryResponse;
import zorvyn.assessment.DTOs.Response.dashboard.DashboardSummaryResponse;
import zorvyn.assessment.DTOs.Response.dashboard.MonthlyTrendResponse;
import zorvyn.assessment.Service.DashboardService;

import java.util.List;


@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard APIs", description = "Endpoints for dashboard data and analytics")

public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get total income, expense and net balance (All roles). Users (viewer and Analyst) can only see their personal summary, while Admin can see overall summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(Authentication authentication) {
        String email=authentication.getName();
        String role=authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(dashboardService.getSummary(email, role));
    }

    @GetMapping("/by-category")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get total by category (Admin and Analyst)")
    public ResponseEntity<List<CategorySummaryResponse>> getSummaryByCategory() {
        return ResponseEntity.ok(dashboardService.getTotalByCategory());
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get monthly trends of year (Admin and Analyst only)")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrends(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year)
    {
        return ResponseEntity.ok(dashboardService.getMonthlyTrends(year));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get 5 recent financial records (All roles). Users (viewer and Analyst) can only see their personal records, while Admin can see all records")
    public ResponseEntity<List<RecordResponse>> getRecentFinancialRecords(Authentication authentication) {
        String email=authentication.getName();
        String role=authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(dashboardService.getRecentRecords(email, role));
    }

}
