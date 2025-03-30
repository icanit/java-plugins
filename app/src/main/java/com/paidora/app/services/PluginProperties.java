package com.paidora.app.services;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "plugin")
public class PluginProperties {
    private String name;
    private Map<String, String> params = new HashMap<>();
}
