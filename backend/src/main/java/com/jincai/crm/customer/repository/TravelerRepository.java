package com.jincai.crm.customer.repository;

import com.jincai.crm.customer.entity.Traveler;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelerRepository extends JpaRepository<Traveler, String> {

    List<Traveler> findByDeletedFalse();

    List<Traveler> findByCustomerIdAndDeletedFalse(String customerId);
}

