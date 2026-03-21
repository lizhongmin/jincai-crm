package com.jincai.crm.integration.repository;

import com.jincai.crm.integration.entity.*;
import com.jincai.crm.integration.service.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboundEventRepository extends JpaRepository<OutboundEvent, Long> {
}

