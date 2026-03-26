package com.jincai.crm.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "crm_traveler_document")
public class TravelerDocument extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveler_id", nullable = false)
    private Traveler traveler;

    @Column(name = "doc_type", nullable = false)
    private String docType;

    @Column(name = "doc_no", nullable = false)
    private String docNo;
}
