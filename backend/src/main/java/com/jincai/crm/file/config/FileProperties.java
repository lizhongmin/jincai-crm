package com.jincai.crm.file.config;

import com.jincai.crm.file.controller.*;
import com.jincai.crm.file.entity.*;
import com.jincai.crm.file.repository.*;
import com.jincai.crm.file.service.*;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.file")
public record FileProperties(String uploadDir) {
}

