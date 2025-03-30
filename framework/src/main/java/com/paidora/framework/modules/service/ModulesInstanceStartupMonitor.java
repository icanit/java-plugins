package com.paidora.framework.modules.service;

import com.paidora.framework.modules.exceptions.ModuleInstanceException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ModulesInstanceStartupMonitor {
    private final Map<Long, String> moduleStartupErrors = new ConcurrentHashMap<>();

    public synchronized String getModuleStartupError(Long instanceId) {
        return moduleStartupErrors.get(instanceId);
    }

    public synchronized void cleanModuleError(Long instanceId) {
        moduleStartupErrors.remove(instanceId);
    }

    public void addModuleStartupError(Long instanceId, ModuleInstanceException e) {
        String reason;
        if (e.getCause() != null) {
            reason = e.getCause().getMessage();
        } else {
            reason = e.getMessage();
        }
        moduleStartupErrors.put(instanceId, Objects.requireNonNullElse(reason,"null"));
    }
}
