package com.inn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.inn.service.MemberService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import com.inn.config.CustomOAuth2UserService; // CustomOAuth2UserService import 추가
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MemberService memberService;
    private final CustomOAuth2UserService customOAuth2UserService; // CustomOAuth2UserService 주입
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(MemberService memberService, CustomOAuth2UserService customOAuth2UserService, AuthenticationEntryPoint authenticationEntryPoint) {
        this.memberService = memberService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 빈 노출
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_MANAGER");
        return roleHierarchy;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationSuccessHandler customAuthenticationSuccessHandler) throws Exception {
        http
                .userDetailsService(memberService)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/admin/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/",
                                "/login",
                                "/signin",
                                "/css/**",
                                "/javascript/**",
                                "/image/**",
                                "/isMember",
                                "/login/oauth2/code/naver",
                                "/qna",
                                "/ws-chat/**",
                                "/h_list",
                                "/h_search",
                                "/sms/auth",
                                "/domestic-accommodations",
                                "/event/eventlist"
                        ).permitAll() // 이 경로들은 인증 없이 접근 허용
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // 로그인 페이지 URL
                        .successHandler(customAuthenticationSuccessHandler) // 로그인 성공 시 리다이렉트할 URL
                        .failureUrl("/login?error=true") // 로그인 실패 시 리다이렉트할 URL
                        .usernameParameter("memberEmail") // 로그인 폼에서 사용자 이름으로 사용할 파라미터 이름
                        .passwordParameter("memberPassword") // 로그인 폼에서 비밀번호로 사용할 파라미터 이름
                )
                .oauth2Login(oauth2Login -> oauth2Login // OAuth2 로그인 설정 추가
                        .loginPage("/login") // OAuth2 로그인 시작 페이지
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // CustomOAuth2UserService 지정
                        )
                        .defaultSuccessUrl("/") // OAuth2 로그인 성공 후 기본 리다이렉트 URL
                        .failureUrl("/login?error=true") // OAuth2 로그인 실패 시 리다이렉트 URL
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/") // 로그아웃 성공 시 리다이렉트할 URL
                        .invalidateHttpSession(true) // HTTP 세션 무효화
                        .deleteCookies("JSESSIONID", "remember-me") // remember-me 쿠키도 삭제
                )
                .rememberMe(rememberMe -> rememberMe
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(86400 * 14) // 14일간 유효
                        .alwaysRemember(false) // 체크박스를 선택해야만 활성화
                        .userDetailsService(memberService)
                )
                .csrf(csrf -> csrf.disable()); // 개발 편의상 CSRF 비활성화 (운영 시 활성화 권장)
        return http.build();
    }
}
