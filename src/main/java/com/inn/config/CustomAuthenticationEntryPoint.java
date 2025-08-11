package com.inn.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String ajaxHeader = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader);

        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"로그인이 필요합니다.\", \"status\": 401}");
        } else {
            String referer = request.getHeader("Referer");
            String returnUrl = request.getRequestURI();

            // 요청 URI에 쿼리 스트링이 있는 경우 함께 포함
            if (request.getQueryString() != null) {
                returnUrl += "?" + request.getQueryString();
            }

            String redirectUrl = UriComponentsBuilder.fromUriString(referer != null ? referer : "/")
                    .queryParam("loginRequired", "true")
                    .queryParam("returnUrl", returnUrl)
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);
        }
    }
}