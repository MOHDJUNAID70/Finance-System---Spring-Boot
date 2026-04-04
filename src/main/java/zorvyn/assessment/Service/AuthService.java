package zorvyn.assessment.Service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zorvyn.assessment.DTOs.Response.AuthResponse;
import zorvyn.assessment.JWT.JwtService;
import zorvyn.assessment.DTOs.Request.LoginUserRequest;
import zorvyn.assessment.DTOs.Request.RegisterUserRequest;
import zorvyn.assessment.Enum.UserStatus;
import zorvyn.assessment.Exception.CustomException;
import zorvyn.assessment.Model.Users;
import zorvyn.assessment.Repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public void register(@Valid RegisterUserRequest request) {

        Users user = new Users();
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse verify(@Valid LoginUserRequest request) {
        Users user=userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("User not found with email: " + request.getEmail()));
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
                request.getPassword()));
        String token=jwtService.generateKey(request.getEmail());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setName(request.getEmail());
        authResponse.setRole(user.getRole());

        return authResponse;
    }
}
