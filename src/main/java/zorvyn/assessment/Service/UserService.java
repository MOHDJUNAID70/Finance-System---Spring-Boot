package zorvyn.assessment.Service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import zorvyn.assessment.DTOs.Request.UpdateUserRequest;
import zorvyn.assessment.DTOs.Response.UserResponse;
import zorvyn.assessment.Enum.Role;
import zorvyn.assessment.Enum.UserStatus;
import zorvyn.assessment.Exception.CustomException;
import zorvyn.assessment.Mapper.UserResponseMapper;
import zorvyn.assessment.Model.Users;
import zorvyn.assessment.Repository.UserRepository;
import zorvyn.assessment.Specification.UserSpecification;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserResponseMapper userResponseMapper;

    public List<UserResponse> getAllUsers() {
        List<Users> user= userRepository.findAll();
        if(user.isEmpty()){
            throw new CustomException("No users found");
        }
        return user.stream().map(userResponseMapper::toUserResponse).toList();
    }

    public UserResponse getUserById(Integer id) {
        Users user=userRepository.findById(id)
                .orElseThrow(()-> new CustomException("User with id "+id+" not found"));
        return userResponseMapper.toUserResponse(user);
    }

    @Transactional
    public void updateUser(Integer id, UpdateUserRequest request) {
        Users user=userRepository.findById(id)
                .orElseThrow(()-> new CustomException("User with id "+id+" not found"));
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Integer id) {
        Users user=userRepository.findById(id).orElseThrow(()-> new CustomException("User with id "+id+" not found"));
        if(user.getStatus() == UserStatus.INACTIVE){
            throw new CustomException("User is already inactive");
        }
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    public Users getOwnProfile(String name) {
        return userRepository.findByEmail(name)
                .orElseThrow(()-> new CustomException("User with email "+name+" not found"));
    }

    public Page<UserResponse> getAllUsersWithPaginationAndFilter(Pageable pageable, String name,
                                                                 String email, Role role, UserStatus status)
    {
        Specification<Users> spec= UserSpecification.getSpecification(name, email, role, status);
        return userRepository.findAll(spec, pageable).map(userResponseMapper::toUserResponse);
    }
}
