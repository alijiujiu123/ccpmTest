package com.cvagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "用户登录响应", title = "登录响应")
public class AuthResponse {

    @Schema(description = "JWT令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @Schema(description = "令牌类型", example = "Bearer", defaultValue = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "用户信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserDto user;

    @Schema(description = "令牌过期时间", example = "2025-09-17T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
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
