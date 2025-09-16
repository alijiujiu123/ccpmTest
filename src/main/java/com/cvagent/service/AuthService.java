package com.cvagent.service;

import com.cvagent.dto.AuthRequest;
import com.cvagent.dto.AuthResponse;
import com.cvagent.dto.RegisterRequest;
import com.cvagent.dto.UserDto;
import com.cvagent.exception.BadRequestException;
import com.cvagent.model.User;
import com.cvagent.repository.UserRepository;
import com.cvagent.security.JwtTokenProvider;
import com.cvagent.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }
    
    public AuthResponse authenticateUser(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(),
                authRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserDto userDto = convertToDto(userPrincipalToUser(userPrincipal));
        
        LocalDateTime expiryDate = tokenProvider.getExpiryDateFromToken(jwt);
        
        return new AuthResponse(jwt, userDto, expiryDate);
    }
    
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("用户名已被使用");
        }
        
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("邮箱已被使用");
        }
        
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setFullName(registerRequest.getFullName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(List.of("USER"));
        
        User savedUser = userRepository.save(user);
        UserDto userDto = convertToDto(savedUser);
        
        // 自动登录新注册用户
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(registerRequest.getUsername());
        authRequest.setPassword(registerRequest.getPassword());
        
        return authenticateUser(authRequest);
    }
    
    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return convertToDto(userPrincipalToUser(userPrincipal));
    }
    
    private User userPrincipalToUser(UserPrincipal userPrincipal) {
        return userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new BadRequestException("用户不存在"));
    }
    
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoles(user.getRoles());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
