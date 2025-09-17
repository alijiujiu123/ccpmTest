package com.cvagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "通用API响应格式", title = "API响应")
public class ApiResponse<T> {

    @Schema(description = "请求是否成功", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean success;

    @Schema(description = "响应数据", anyOf = {Object.class, String.class, Integer.class, Boolean.class})
    private T data;

    @Schema(description = "成功消息", example = "操作成功")
    private String message;

    @Schema(description = "错误信息", example = "参数错误")
    private String error;

    // 成功响应构造函数
    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // 成功响应构造函数（无消息）
    public ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    // 错误响应构造函数
    public ApiResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // 空构造函数
    public ApiResponse() {}

    // 静态工厂方法
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(false, error);
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}