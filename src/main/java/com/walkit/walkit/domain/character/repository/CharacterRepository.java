package com.walkit.walkit.domain.character.repository;

import com.walkit.walkit.domain.character.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterRepository extends JpaRepository<Character, Long> {
}
