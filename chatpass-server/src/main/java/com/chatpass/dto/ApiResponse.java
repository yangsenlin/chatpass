package com.chatpass.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 统一 API 响应封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private T result;
    private String msg;
    private String code;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .result(data)
                .msg("success")
                .code("SUCCESS")
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .result(data)
                .msg(message)
                .code("SUCCESS")
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .result(null)
                .msg(message)
                .code(code)
                .build();
    }
}