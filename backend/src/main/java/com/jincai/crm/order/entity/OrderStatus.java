package com.jincai.crm.order.entity;

import com.jincai.crm.order.controller.*;
import com.jincai.crm.order.dto.*;
import com.jincai.crm.order.repository.*;
import com.jincai.crm.order.service.*;

public enum OrderStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    FINANCE_IN_PROGRESS,
    COMPLETED,
    CANCELED
}

