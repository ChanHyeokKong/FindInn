package com.inn.data.test;

import com.inn.data.test.CharacterType;
import java.util.List; // List를 import 합니다.
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterTypeRepository extends JpaRepository<CharacterType, Long> {
    Optional<CharacterType> findFirstByTraitOrderByIdxAsc(String trait);
    Optional<CharacterType> findByTrait(String trait);
}