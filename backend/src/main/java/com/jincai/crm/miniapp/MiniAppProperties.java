package com.jincai.crm.miniapp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.miniapp")
public record MiniAppProperties(
    String appId,
    String secret,
    boolean mockEnabled,
    String mockCodePrefix
) {
}
