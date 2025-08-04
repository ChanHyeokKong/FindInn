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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
        return roleHierarchy;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/admin/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/",
                                "/login",
                                "/signup",
                                "/signup2",
                                "/css/**",
                                "/JavaScript/**",
                                "/image/**",
                                "/isMember" // /isMember도 permitAll에 추가하는 것이 좋습니다.
                        ).permitAll() // 이 경로들은 인증 없이 접근 허용
                        .anyRequest().permitAll() // 그 외 모든 요청은 인증 필요
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // 로그인 페이지 URL
                        .defaultSuccessUrl("/", true) // 로그인 성공 시 리다이렉트할 URL
                        .failureUrl("/login?error=true") // 로그인 실패 시 리다이렉트할 URL
                        .usernameParameter("m_email") // 로그인 폼에서 사용자 이름으로 사용할 파라미터 이름
                        .passwordParameter("m_password") // 로그인 폼에서 비밀번호로 사용할 파라미터 이름
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/") // 로그아웃 성공 시 리다이렉트할 URL
                        .invalidateHttpSession(true) // HTTP 세션 무효화
                        .deleteCookies("JSESSIONID") // 쿠키 삭제
                )
                .csrf(csrf -> csrf.disable()); // 개발 편의상 CSRF 비활성화 (운영 시 활성화 권장)
        return http.build();
    }
}
