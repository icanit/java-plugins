package com.paidora.app.services.plugin;

import com.paidora.app.services.PluginProperties;
import com.paidora.framework.modules.exceptions.ModuleInstanceException;
import com.paidora.framework.modules.service.ModulesInstanceStartupMonitor;
import com.paidora.modules.lib.IPlugin;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
public class PluginsManager implements SmartLifecycle {

    private final PluginInstanceManager instanceManager;
    private final ModulesInstanceStartupMonitor startupMonitor;
    private final PluginProperties pluginProperties;
    @SuppressWarnings("unused")
    private final Object startStopLock = new Object();
    protected boolean isRunning;

    @Autowired
    public PluginsManager(PluginInstanceManager instanceManager,
                          @Qualifier("pluginStartupMonitor") ModulesInstanceStartupMonitor startupMonitor,
                          PluginProperties pluginProperties) {
        this.instanceManager = instanceManager;
        this.startupMonitor = startupMonitor;
        this.pluginProperties = pluginProperties;
    }

    @Override
    @Synchronized("startStopLock")
    public void start() {
        startPlugin(1L);
        isRunning = true;
    }

    @Override
    @Synchronized("startStopLock")
    public void stop() {
        try {
            instanceManager.removeAllModuleInstances();
            isRunning = false;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    public Collection<Long> getStartedPlugins() {
        return instanceManager.getInstanceIds();
    }

    public String getPluginLastStartupError(Long systemId) {
        return startupMonitor.getModuleStartupError(systemId);
    }


    public IPlugin getModule(Long payoutSystemId) {
        var moduleInstance = instanceManager.getModuleInstance(payoutSystemId);
        if (moduleInstance != null) {
            return moduleInstance.getModule();
        }
        return null;
    }


    @Synchronized("startStopLock")
    public void startPlugin(Long pluginId) {
        if (instanceManager.getModuleInstance(pluginId) == null) {
            try {
                instanceManager.createModuleInstance(pluginId, pluginProperties.getName());
                startupMonitor.cleanModuleError(pluginId);
            } catch (ModuleInstanceException e) {
                log.error("Start Plugin module error for system: " + pluginId, e);
                startupMonitor.addModuleStartupError(pluginId, e);
            }
        }
    }

    @Synchronized("startStopLock")
    public void stopPlugin(Long pluginId) {
        try {
            instanceManager.removeModuleInstance(pluginId);
            startupMonitor.cleanModuleError(pluginId);
        } catch (ModuleInstanceException e) {
            log.error("Stop PayoutSystem Module error for system: " + pluginId, e);
        }
    }

    @Synchronized("startStopLock")
    public boolean restartPlugin(Long pluginId) {
        try {
            if (instanceManager.getModuleInstance(pluginId) == null) {
                startPlugin(pluginId);
            } else {
                instanceManager.reloadModule(pluginId);
                startupMonitor.cleanModuleError(pluginId);
            }
            return true;
        } catch (ModuleInstanceException e) {
            log.error("Restart PayoutSystem for system: " + pluginId + " not started.", e);
            startupMonitor.addModuleStartupError(pluginId, e);
        }
        return false;
    }
}
