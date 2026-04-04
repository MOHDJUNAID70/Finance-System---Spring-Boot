package zorvyn.assessment.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import zorvyn.assessment.DTOs.Request.UpdateUserRequest;
import zorvyn.assessment.DTOs.Response.UserResponse;
import zorvyn.assessment.Enum.Role;
import zorvyn.assessment.Enum.UserStatus;
import zorvyn.assessment.Model.Users;
import zorvyn.assessment.Service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management APIs", description = "APIs for managing users — Admin only")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/all_users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users with Pagination and Filter (Admin only)", operationId = "1_All")
    public ResponseEntity<Page<UserResponse>> getAllUsersWithPagination(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status)
    {
        Sort sort=Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page-1, size, sort);
        return ResponseEntity.ok(userService.getAllUsersWithPaginationAndFilter(pageable, name, email, role, status));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (Admin only)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user by ID (Admin only)")
    public ResponseEntity<String> updateUserById(@PathVariable Integer id, @RequestBody UpdateUserRequest updateUserRequest) {
        userService.updateUser(id, updateUserRequest);
        return new ResponseEntity<>("User has been updated Successfully",HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a user")
    public ResponseEntity<String> deactivateUser(@PathVariable Integer id) {
        userService.deactivateUser(id);
        return new ResponseEntity<>("User has been deactivated",HttpStatus.OK);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get my profile (All roles)")
    public ResponseEntity<Users> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getOwnProfile(authentication.getName()));
    }
}
