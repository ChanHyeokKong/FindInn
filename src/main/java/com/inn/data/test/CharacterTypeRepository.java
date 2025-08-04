package com.inn.data.test;

package com.inn.data.test;

import com.inn.data.test.CharacterType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterTypeRepository extends JpaRepository<CharacterType, Long> {
    CharacterType findByTrait(String trait);
}