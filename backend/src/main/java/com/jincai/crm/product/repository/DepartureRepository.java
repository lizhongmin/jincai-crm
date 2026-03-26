package com.jincai.crm.product.repository;

import com.jincai.crm.product.entity.Departure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DepartureRepository extends JpaRepository<Departure, String>, JpaSpecificationExecutor<Departure> {

    List<Departure> findByDeletedFalse();

    List<Departure> findByRouteIdAndDeletedFalse(String routeId);

    boolean existsByCode(String code);
}
