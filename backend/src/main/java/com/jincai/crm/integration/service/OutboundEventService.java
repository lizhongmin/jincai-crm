package com.jincai.crm.integration.service;

import com.jincai.crm.integration.entity.*;
import com.jincai.crm.integration.repository.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class OutboundEventService {

    private final OutboundEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboundEventService(OutboundEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void publish(String eventType, String bizType, Long bizId, Object payload) {
        OutboundEvent event = new OutboundEvent();
        event.setEventType(eventType);
        event.setBizType(bizType);
        event.setBizId(bizId);
        try {
            event.setPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            event.setPayload("{}");
        }
        repository.save(event);
    }
}

