package com.cvagent.controller;

import com.cvagent.dto.AuthRequest;
import com.cvagent.dto.AuthResponse;
import com.cvagent.dto.RegisterRequest;
import com.cvagent.dto.UserDto;
import com.cvagent.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户登录、注册和认证相关接口")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码进行用户登录认证")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "认证失败")
    })
    public ResponseEntity<AuthResponse> authenticateUser(
            @Parameter(description = "登录请求参数", required = true)
            @Valid @RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.authenticateUser(authRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账户")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注册成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "用户名已存在")
    })
    public ResponseEntity<AuthResponse> registerUser(
            @Parameter(description = "注册请求参数", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse authResponse = authService.registerUser(registerRequest);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto userDto = authService.getCurrentUser();
        return ResponseEntity.ok(userDto);
    }
}
