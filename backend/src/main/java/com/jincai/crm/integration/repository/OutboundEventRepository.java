package com.jincai.crm.integration.repository;

import com.jincai.crm.integration.entity.OutboundEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboundEventRepository extends JpaRepository<OutboundEvent, String> {
}

