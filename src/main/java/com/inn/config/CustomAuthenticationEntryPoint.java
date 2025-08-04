package com.inn.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        String requestUri = request.getRequestURI();

        // If the request is already for the login page, do not redirect again to avoid loop
        // Instead, send a 401 error. The client-side JS will then handle showing the modal.
        if ("/login".equals(requestUri)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - Already on login page");
            return;
        }

        // AJAX 요청인지 확인 (X-Requested-With 헤더는 jQuery, Axios 등에서 자동으로 추가)
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            // AJAX 요청이면 401 Unauthorized 응답
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        } else {
            // 일반 요청이면 로그인 페이지로 리다이렉트
            response.sendRedirect("/login");
        }
    }
}
