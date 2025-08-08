package com.inn.config;

import com.inn.data.member.MemberDto;
import com.inn.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // naver, kakao 등
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String socialProvider = registrationId;
        String socialProviderKey = null;
        String email = null;
        String name = null;
        String mobile = null;

        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            socialProviderKey = (String) response.get("id");
            email = (String) response.get("email");
            name = (String) response.get("name");
            mobile = (String) response.get("mobile");
        } else if ("kakao".equals(registrationId)) {
            socialProviderKey = String.valueOf(attributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
                // 사용자님의 지시에 따라 'account_name'을 이름으로 사용합니다.
                name = (String) kakaoAccount.get("name");
                // 카카오 전화번호 정보 파싱 추가
                mobile = (String) kakaoAccount.get("phone_number");
            }
        }

        // MemberService를 통해 사용자 정보 처리 (저장 또는 업데이트)
        CustomUserDetails customUserDetails = (CustomUserDetails) memberService.processSocialLogin(socialProvider, socialProviderKey, email, name, mobile);

        return customUserDetails;
    }
}
