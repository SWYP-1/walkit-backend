package com.walkit.walkit.domain.item.repository;

import com.walkit.walkit.domain.item.entity.Item;
import com.walkit.walkit.domain.item.entity.ItemManagement;
import com.walkit.walkit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemManagementRepository extends JpaRepository<ItemManagement, Long> {
    Optional<ItemManagement> findByUserAndItem(User user, Item item);
    boolean existsByUserAndItem(User user, Item item);
}
