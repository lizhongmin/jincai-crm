package com.jincai.crm.miniapp.repository;

import com.jincai.crm.miniapp.entity.MiniAppUserBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MiniAppUserBindingRepository extends JpaRepository<MiniAppUserBinding, String> {

    Optional<MiniAppUserBinding> findByOpenIdAndDeletedFalse(String openId);

    Optional<MiniAppUserBinding> findByUserIdAndDeletedFalse(String userId);

    boolean existsByUserIdAndDeletedFalse(String userId);
}
