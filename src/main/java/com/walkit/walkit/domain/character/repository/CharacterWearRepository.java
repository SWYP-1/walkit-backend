package com.walkit.walkit.domain.character.repository;

import com.walkit.walkit.domain.item.entity.Item;
import com.walkit.walkit.domain.character.entity.Character;
import com.walkit.walkit.domain.character.entity.CharacterWear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterWearRepository extends JpaRepository<CharacterWear, Long> {
    void deleteByCharacterAndItem(Character character, Item item);

    List<CharacterWear> findByCharacter(Character character);
}
