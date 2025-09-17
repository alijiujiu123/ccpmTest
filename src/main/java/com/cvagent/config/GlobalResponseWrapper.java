package com.cvagent.config;

import com.cvagent.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 不包装已经包装过的响应和错误响应
        return !returnType.getParameterType().equals(ResponseEntity.class) &&
               !returnType.getParameterType().equals(ApiResponse.class) &&
               !returnType.getParameterType().equals(String.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 如果返回的是ResponseEntity，提取其中的body
        if (body instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) body;
            Object data = responseEntity.getBody();
            if (data instanceof ApiResponse) {
                return data; // 已经是ApiResponse格式，直接返回
            }
            return ApiResponse.success(data);
        }

        // 包装其他所有响应
        return ApiResponse.success(body);
    }
}