package com.cvagent.dto;

import java.time.LocalDateTime;

public class AuthResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private UserDto user;
    private LocalDateTime expiresIn;
    
    // 构造函数
    public AuthResponse(String token, UserDto user, LocalDateTime expiresIn) {
        this.token = token;
        this.user = user;
        this.expiresIn = expiresIn;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    
    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }
    
    public LocalDateTime getExpiresIn() { return expiresIn; }
    public void setExpiresIn(LocalDateTime expiresIn) { this.expiresIn = expiresIn; }
}
