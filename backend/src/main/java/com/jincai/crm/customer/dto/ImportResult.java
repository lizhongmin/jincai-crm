package com.jincai.crm.customer.dto;

import com.jincai.crm.customer.controller.*;
import com.jincai.crm.customer.entity.*;
import com.jincai.crm.customer.repository.*;
import com.jincai.crm.customer.service.*;

import java.util.List;

public record ImportResult(
    int success,
    int failed,
    List<String> errors
) {
}

