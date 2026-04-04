package zorvyn.assessment.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import zorvyn.assessment.DTOs.Request.RecordRequest;
import zorvyn.assessment.DTOs.Response.RecordResponse;
import zorvyn.assessment.Enum.RecordType;
import zorvyn.assessment.Service.RecordService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/record")
@Tag(name = "Financial Record APIs", description = "APIs for managing records — Admin and User")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @GetMapping("/All_Record")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all financial records with pagination and Filter (Admin only)")
    public Page<RecordResponse> getAllRecords(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false,  defaultValue = "5") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) Integer minAmount,
            @RequestParam(required = false) Integer maxAmount,
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) boolean deleted
    ){
        Sort sort=Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page-1, size, sort);
        return recordService.getAllRecordWithPaginationAndFilter(pageable, minAmount, maxAmount,
                type, category, deleted);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all financial records (Admin only)")
    public List<RecordResponse> getAllRecords() {
        return recordService.getAllRecord();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Create a new financial record with idempotency check (Admin, Analyst only)")
    public ResponseEntity<String> createRecord(@RequestHeader("Idem-key") String key, @RequestBody @Valid RecordRequest recordRequest,
                                               Authentication authentication) {
        return recordService.createRecordWithIdempotencyCheck(key, recordRequest, authentication);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a financial record by ID (All roles). Users can only access their records")
    public ResponseEntity<RecordResponse> getRecordById(@PathVariable Integer id, Authentication authentication) {
        return ResponseEntity.ok(recordService.getRecordById(id, authentication));
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all financial records created by a specific user (Admin only)")
    public List<RecordResponse> getRecordsByUserId(@PathVariable Integer id) {
        return recordService.getAllRecordByUser(id);
    }

    @PutMapping("/updateRecord/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Update a financial record (Admin, Analyst only)")
    public ResponseEntity<String> updateRecord(@PathVariable Integer id, @RequestBody RecordRequest recordRequest) {
        recordService.updateRecord(id, recordRequest);
        return new ResponseEntity<>("Record updated successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete (Soft delete) a financial record (Admin only)")
    public ResponseEntity<String> deleteRecord(@PathVariable Integer id) {
        recordService.deleteRecordById(id);
        return new ResponseEntity<>("Record Deleted Successfully", HttpStatus.OK);
    }

}
