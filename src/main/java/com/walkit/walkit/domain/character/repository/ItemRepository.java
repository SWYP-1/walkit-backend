package com.walkit.walkit.domain.character.repository;

import com.walkit.walkit.domain.character.entity.Item;
import com.walkit.walkit.domain.character.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByPosition(Position position);
}
