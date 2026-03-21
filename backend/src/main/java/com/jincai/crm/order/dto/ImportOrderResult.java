package com.jincai.crm.order.dto;

import com.jincai.crm.order.controller.*;
import com.jincai.crm.order.entity.*;
import com.jincai.crm.order.repository.*;
import com.jincai.crm.order.service.*;

import java.util.List;

public record ImportOrderResult(
    int success,
    int failed,
    List<String> errors
) {
}

