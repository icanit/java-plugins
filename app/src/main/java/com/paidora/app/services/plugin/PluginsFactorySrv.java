package com.paidora.app.services.plugin;

import com.paidora.framework.modules.service.ModulesFactorySrvBase;
import com.paidora.modules.Plugins;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;


@Service
public class PluginsFactorySrv extends ModulesFactorySrvBase {

    protected PluginsFactorySrv(PluginsStorageManager storageManager) {
        super(Plugins.getPluginsRoot(), storageManager);
    }

    @PostConstruct
    protected void init() {
        super.loadModulesFromDir();
    }
}
