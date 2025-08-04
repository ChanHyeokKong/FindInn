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

public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final String LAST_PAGE_URL_SESSION_KEY = "lastPageUrl"; // 상수 추가

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {

        HttpSession session = request.getSession();
        String lastPageUrl = (String) session.getAttribute(LAST_PAGE_URL_SESSION_KEY);

        // 1. LastPageInterceptor에 의해 저장된 URL이 있다면 그곳으로 리다이렉트
        if (lastPageUrl != null && !lastPageUrl.isEmpty()) {
            session.removeAttribute(LAST_PAGE_URL_SESSION_KEY); // 사용 후 세션에서 제거
            getRedirectStrategy().sendRedirect(request, response, lastPageUrl);
            return;
        }

        // 2. SavedRequestAwareAuthenticationSuccessHandler에 의해 저장된 요청이 있다면 그곳으로 리다이렉트
        // (보호된 리소스에 직접 접근하려다 로그인 페이지로 리다이렉트된 경우)
        if (request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST") != null) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        // 3. 위 두 가지 경우가 아니라면 역할 기반 리다이렉트 로직 수행
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = "/"; // 기본 리다이렉트 URL

        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard"; // 관리자 역할이면 관리자 대시보드로
                break;
            } else if (grantedAuthority.getAuthority().equals("ROLE_USER")) {
                redirectUrl = "/user/mypage"; // 사용자 역할이면 마이페이지로
                break;
            }
            // 다른 역할에 대한 리다이렉트 로직 추가 가능
        }

        response.sendRedirect(redirectUrl);
    }
}
