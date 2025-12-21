package com.walkit.walkit.domain.character.repository;

import com.walkit.walkit.domain.character.entity.Item;
import com.walkit.walkit.domain.character.entity.ItemManagement;
import com.walkit.walkit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemManagementRepository extends JpaRepository<ItemManagement, Long> {
    Optional<ItemManagement> findByUserAndItem(User user, Item item);
    boolean existsByUserAndItem(User user, Item item);
}
