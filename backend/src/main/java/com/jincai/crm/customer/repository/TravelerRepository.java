package com.jincai.crm.customer.repository;

import com.jincai.crm.customer.entity.Traveler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TravelerRepository extends JpaRepository<Traveler, String>, JpaSpecificationExecutor<Traveler> {

    List<Traveler> findByDeletedFalse();

    List<Traveler> findByCustomerIdAndDeletedFalse(String customerId);
}
