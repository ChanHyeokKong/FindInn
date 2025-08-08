package com.inn.config;

import com.inn.data.member.MemberDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User; // OAuth2User import 추가

import java.util.Collection;
import java.util.Collections; // Collections import 추가
import java.util.HashMap; // HashMap import 추가
import java.util.Map; // Map import 추가
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails, OAuth2User { // OAuth2User 인터페이스 구현

    private MemberDto member;
    private Map<String, Object> attributes; // OAuth2User 속성을 저장할 필드

    // 일반 로그인용 생성자
    public CustomUserDetails(MemberDto member) {
        this.member = member;
        this.attributes = Collections.emptyMap(); // 기본값으로 빈 Map 설정
    }

    // OAuth2 로그인용 생성자
    public CustomUserDetails(MemberDto member, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
    }

    public MemberDto getMember() {
        return this.member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return member.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return member.getMemberPassword();
    }

    @Override
    public String getUsername() {
        return member.getMemberEmail();
    }

    public String getMemberName() {
        return member.getMemberName();
    }

    public Long getIdx() {
        return member.getIdx();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // OAuth2User 인터페이스 구현 메서드
    @Override
    public Map<String, Object> getAttributes() {
        // MemberDto의 정보를 Map 형태로 반환하거나, OAuth2UserRequest에서 받은 attributes를 반환
        // 여기서는 OAuth2UserRequest에서 받은 attributes를 우선적으로 반환하도록 합니다.
        if (this.attributes != null && !this.attributes.isEmpty()) {
            return this.attributes;
        }
        // 만약 attributes가 없다면, MemberDto의 기본 정보를 Map으로 구성하여 반환
        Map<String, Object> memberAttributes = new HashMap<>();
        memberAttributes.put("id", member.getIdx());
        memberAttributes.put("email", member.getMemberEmail());
        memberAttributes.put("name", member.getMemberName());
        memberAttributes.put("mobile", member.getMemberPhone());
        // 필요한 다른 MemberDto 필드들을 여기에 추가
        return memberAttributes;
    }

    @Override
    public String getName() {
        // OAuth2User의 고유 식별자를 반환합니다.
        // 여기서는 memberEmail을 사용하거나, socialProviderKey를 사용할 수 있습니다.
        // Spring Security의 기본 동작을 고려하여 email을 반환하도록 하겠습니다.
        return member.getMemberEmail();
    }
}
