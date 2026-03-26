package com.jincai.crm.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.file")
public record FileProperties(String uploadDir) {
}

