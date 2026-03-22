package com.jincai.crm.product.repository;

import com.jincai.crm.product.controller.*;
import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.entity.*;
import com.jincai.crm.product.service.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteProductRepository extends JpaRepository<RouteProduct, Long> {

    List<RouteProduct> findByDeletedFalse();

    boolean existsByCode(String code);
}
