package zorvyn.assessment.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import zorvyn.assessment.DTOs.Response.RecordResponse;
import zorvyn.assessment.DTOs.Response.dashboard.CategorySummaryResponse;
import zorvyn.assessment.DTOs.Response.dashboard.DashboardSummaryResponse;
import zorvyn.assessment.DTOs.Response.dashboard.MonthlyTrendResponse;
import zorvyn.assessment.Enum.RecordType;
import zorvyn.assessment.Mapper.RecordResponseMapper;
import zorvyn.assessment.Model.Record;
import zorvyn.assessment.Model.Users;
import zorvyn.assessment.Repository.RecordRepository;
import zorvyn.assessment.Repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private RecordResponseMapper recordResponseMapper;

    @Autowired
    private UserRepository userRepository;

    public DashboardSummaryResponse getSummary(String email, String role) {
        if(role.equals("ROLE_ADMIN")){
            return getAllSummary();
        } else {
            Users user=userRepository.findByEmail(email).orElseThrow();
            BigDecimal totalIncome=recordRepository.sumByTypeAndCreatedBy(RecordType.INCOME, user);
            BigDecimal totalExpense=recordRepository.sumByTypeAndCreatedBy(RecordType.EXPENSE, user);

            totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
            totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

            BigDecimal netBalance=totalIncome.subtract(totalExpense);

            DashboardSummaryResponse summary=new DashboardSummaryResponse();
            summary.setTotalIncome(totalIncome);
            summary.setTotalExpense(totalExpense);
            summary.setNetBalance(netBalance);
            summary.setScope("Personal");
            return summary;
        }
    }

    public DashboardSummaryResponse getAllSummary() {
        BigDecimal totalIncome=recordRepository.sumByType(RecordType.INCOME);
        BigDecimal totalExpense=recordRepository.sumByType(RecordType.EXPENSE);

        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        BigDecimal netBalance=totalIncome.subtract(totalExpense);

        DashboardSummaryResponse summary=new DashboardSummaryResponse();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setNetBalance(netBalance);
        summary.setScope("Global");
        return summary;
    }

    public List<CategorySummaryResponse> getTotalByCategory() {
        List<Object[]> total=recordRepository.sumByCategory();
        return total.stream().map(
                row -> CategorySummaryResponse.builder()
                        .category((String) row[0])
                        .total((BigDecimal) row[1])
                        .build()
        ).toList();
    }

    public List<MonthlyTrendResponse> getMonthlyTrends(int year) {
        List<Object[]> total=recordRepository.monthlyTrends(year);
        return total.stream().map(
                row -> MonthlyTrendResponse.builder()
                        .month((Integer) row[0])
                        .amount((BigDecimal) row[1])
                        .type((RecordType) row[2])
                        .build()
        ).toList();
    }

    public List<RecordResponse> getRecentRecords(String email, String role) {
        if(role.equals("ROLE_ADMIN")){
            return getRecentRecord();
        }
        else{
            Users user=userRepository.findByEmail(email).orElseThrow();
            List<Record> record=recordRepository.findTop5ByCreatedByAndDeletedFalseOrderByCreatedAtDesc(user);
            return record.stream().map(recordResponseMapper::toRecordResponse).toList();
        }
    }

    public List<RecordResponse> getRecentRecord() {
        List<Record> record=recordRepository.findTop5ByDeletedFalseOrderByCreatedAtDesc();
        return record.stream().map(recordResponseMapper::toRecordResponse).toList();
    }
}
