package com.jincai.crm.product.repository;

import com.jincai.crm.product.entity.RouteProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RouteProductRepository extends JpaRepository<RouteProduct, String>, JpaSpecificationExecutor<RouteProduct> {

    List<RouteProduct> findByDeletedFalse();

    boolean existsByCode(String code);
}
