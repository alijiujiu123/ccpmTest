package com.cvagent.controller;

import com.cvagent.dto.AuthRequest;
import com.cvagent.dto.AuthResponse;
import com.cvagent.dto.RegisterRequest;
import com.cvagent.dto.UserDto;
import com.cvagent.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequest authRequest;
    private RegisterRequest registerRequest;
    private AuthResponse authResponse;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setFullName("新用户");
        registerRequest.setPassword("password123");

        userDto = new UserDto();
        userDto.setId("1");
        userDto.setUsername("testuser");
        userDto.setEmail("testuser@example.com");
        userDto.setFullName("测试用户");

        authResponse = new AuthResponse("jwt-token", userDto, LocalDateTime.now().plusHours(1));
    }

    @Test
    void authenticateUser_成功登录() throws Exception {
        // 模拟服务层返回
        when(authService.authenticateUser(any(AuthRequest.class))).thenReturn(authResponse);

        // 执行测试
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("testuser@example.com"));
    }

    @Test
    void authenticateUser_用户名不能为空() throws Exception {
        // 创建无效请求（用户名为空）
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setPassword("password123");

        // 执行测试
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticateUser_密码不能为空() throws Exception {
        // 创建无效请求（密码为空）
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setUsername("testuser");

        // 执行测试
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_成功注册() throws Exception {
        // 模拟服务层返回
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(authResponse);

        // 执行测试
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void registerUser_用户名不能为空() throws Exception {
        // 创建无效请求（用户名为空）
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("newuser@example.com");
        invalidRequest.setFullName("新用户");
        invalidRequest.setPassword("password123");

        // 执行测试
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_邮箱格式不正确() throws Exception {
        // 创建无效请求（邮箱格式不正确）
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setUsername("newuser");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setFullName("新用户");
        invalidRequest.setPassword("password123");

        // 执行测试
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_密码长度至少6个字符() throws Exception {
        // 创建无效请求（密码太短）
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setUsername("newuser");
        invalidRequest.setEmail("newuser@example.com");
        invalidRequest.setFullName("新用户");
        invalidRequest.setPassword("123");

        // 执行测试
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUser_成功获取当前用户() throws Exception {
        // 模拟服务层返回
        when(authService.getCurrentUser()).thenReturn(userDto);

        // 执行测试
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("测试用户"));
    }

    @Test
    void getCurrentUser_未认证用户() throws Exception {
        // 执行测试（未认证用户）
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}