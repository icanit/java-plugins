package com.paidora.app.config;

import com.paidora.framework.modules.service.ModulesInstanceStartupMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaemonConfig {
    @Bean("pluginStartupMonitor")
    public ModulesInstanceStartupMonitor getPsStartupMonitor() {
        return new ModulesInstanceStartupMonitor();
    }
}
