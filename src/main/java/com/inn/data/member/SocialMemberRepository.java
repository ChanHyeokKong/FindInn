package com.inn.data.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialMemberRepository extends JpaRepository<SocialMemberDto, Long> {
    Optional<SocialMemberDto> findBySocialProviderAndSocialProviderKey(String socialProvider, String socialProviderKey);
}
