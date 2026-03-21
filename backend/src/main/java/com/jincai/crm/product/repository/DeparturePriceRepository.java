package com.jincai.crm.product.repository;

import com.jincai.crm.product.controller.*;
import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.entity.*;
import com.jincai.crm.product.service.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeparturePriceRepository extends JpaRepository<DeparturePrice, Long> {

    List<DeparturePrice> findByDepartureIdAndDeletedFalse(Long departureId);
}

