package com.inn.data.test;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterTypeRepository extends JpaRepository<CharacterType, Long> {

    /** 권장: 대소문자 무시로 단일 경로 통일 */
    Optional<CharacterType> findFirstByTraitIgnoreCase(String trait);

}