package com.paidora.app.services.plugin;

import com.paidora.framework.modules.service.ModulesUpdateManagerBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PluginsUpdateManager extends ModulesUpdateManagerBase {
    private final boolean update;

    public PluginsUpdateManager(PluginsRepositoryProperties repositoryProperties,
                                PluginsFactorySrv factorySrv,
                                @Value("${plugins.update}") boolean update
    ) {
        super("plugins-", repositoryProperties, factorySrv);
        this.update = update;
    }

    @Scheduled(fixedDelay = 60 * 1000)
    protected void regularUpdate() {
        if (update) {
            super.loadModulesFromRepository();
        }
    }
}
