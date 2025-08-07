package com.inn.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // 이 import 추가
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ...

public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    private static final String LAST_PAGE_URL_SESSION_KEY = "lastPageUrl"; // 상수 추가

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        HttpSession session = request.getSession();

        // 디버깅 로그 추가
        logger.info("Authentication successful for user: {}", authentication.getName());
        logger.info("User authorities: {}", authorities);

        // 1. 역할 기반 리다이렉션 (관리자 우선)
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
                // 관리자는 항상 관리자 대시보드로 이동
                getRedirectStrategy().sendRedirect(request, response, "/admin/memberlist");
                return; // 리다이렉션 후 즉시 종료
            }
            else if (grantedAuthority.getAuthority().equals("ROLE_MANAGER")) {
                // 관리자는 항상 관리자 대시보드로 이동
                getRedirectStrategy().sendRedirect(request, response, "/manage/hotel");
                return; // 리다이렉션 후 즉시 종료
            }
        }

        // 2. LastPageInterceptor에 의해 저장된 URL 확인
        String lastPageUrl = (String) session.getAttribute(LAST_PAGE_URL_SESSION_KEY);
        if (lastPageUrl != null && !lastPageUrl.isEmpty()) {
            session.removeAttribute(LAST_PAGE_URL_SESSION_KEY); // 사용 후 세션에서 제거
            getRedirectStrategy().sendRedirect(request, response, lastPageUrl);
            return;
        }

        // 3. SavedRequestAwareAuthenticationSuccessHandler의 기본 동작 (보호된 페이지 접근 시)
        if (request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST") != null) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        // 4. 위 모든 경우에 해당하지 않을 때 기본 URL로 리다이렉트
        String redirectUrl = "/"; // 기본 리다이렉트 URL
         for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("ROLE_USER")) {
                redirectUrl = "/"; // 일반 사용자는 메인 페이지로
                break;
            }
        }
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
