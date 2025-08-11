package com.inn.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LastPageInterceptor implements HandlerInterceptor {

    private static final String LAST_PAGE_URL_SESSION_KEY = "lastPageUrl";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        HttpSession session = request.getSession();

        // 로그인, 로그아웃, 회원가입, 정적 리소스, 에러 페이지 등은 저장하지 않음
        if (!requestUri.startsWith("/login") &&
            !requestUri.startsWith("/logout") &&
            !requestUri.startsWith("/signup") &&
            !requestUri.startsWith("/css/") &&
            !requestUri.startsWith("/javascript/") &&
            !requestUri.startsWith("/image/") &&
            !requestUri.startsWith("/error") &&
            !requestUri.startsWith("/isMember") &&
            !requestUri.startsWith("/favicon.ico"))
        {

            session.setAttribute(LAST_PAGE_URL_SESSION_KEY, requestUri);
        }
        return true;
    }
}
