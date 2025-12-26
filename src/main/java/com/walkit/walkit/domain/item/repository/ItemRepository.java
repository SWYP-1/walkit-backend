package com.walkit.walkit.domain.item.repository;

import com.walkit.walkit.domain.item.entity.Item;
import com.walkit.walkit.domain.item.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByPosition(Position position);
}
