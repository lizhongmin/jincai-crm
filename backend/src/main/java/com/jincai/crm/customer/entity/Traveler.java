package com.jincai.crm.customer.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "crm_traveler")
public class Traveler extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String name;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "id_no")
    private String idNo;

    private LocalDate birthday;

    @Column(name = "gender")
    private String gender;

    @Column(name = "ethnicity")
    private String ethnicity;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "address")
    private String address;

    private String phone;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "emergency_phone")
    private String emergencyPhone;

    @Column(name = "preferences", length = 1000)
    private String preferences;

    @OneToMany(mappedBy = "traveler", fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<TravelerDocument> documents = new ArrayList<>();
}
