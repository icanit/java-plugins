package com.paidora.app.services.plugin;

import com.paidora.framework.modules.service.ModulesStorageManagerBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PluginsStorageManager extends ModulesStorageManagerBase {

    protected PluginsStorageManager(@Value("${plugins.dir}") String pluginsDir) {
        super(pluginsDir);
    }
}
