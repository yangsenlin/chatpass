package com.chatpass.filter;

import com.chatpass.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Rate Limit Filter
 * 
 * API 请求频率限制过滤器
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter implements Filter {

    private final RateLimitConfig rateLimitConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取请求类型
        String requestType = getRequestType(httpRequest);
        
        // 获取对应的限流 Bucket
        Bucket bucket = rateLimitConfig.getBucket(requestType);
        
        if (bucket == null) {
            // 未配置限流的请求类型，直接通过
            chain.doFilter(request, response);
            return;
        }

        // 检查是否允许请求
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            // 请求被拒绝
            log.warn("Rate limit exceeded for request type: {}", requestType);
            
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write(
                "{\"code\":429,\"message\":\"请求频率超限，请稍后再试\",\"data\":null}"
            );
        }
    }

    /**
     * 根据请求路径判断请求类型
     */
    private String getRequestType(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 消息发送
        if (path.contains("/messages") && "POST".equals(method)) {
            return "message";
        }

        // 文件上传
        if (path.contains("/upload") && "POST".equals(method)) {
            return "upload";
        }

        // 登录
        if (path.contains("/auth/login") || path.contains("/login")) {
            return "login";
        }

        // 其他 API
        if (path.startsWith("/api/")) {
            return "api";
        }

        return null; // 不限流
    }
}