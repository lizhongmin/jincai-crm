package com.jincai.crm.product.repository;

import com.jincai.crm.product.entity.DeparturePrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeparturePriceRepository extends JpaRepository<DeparturePrice, String> {

    List<DeparturePrice> findByDepartureIdAndDeletedFalse(String departureId);
}

