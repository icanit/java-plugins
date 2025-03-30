package com.paidora.app.services.plugin;

import com.paidora.framework.modules.service.ModulesInstanceManagerBase;
import com.paidora.modules.lib.IPlugin;
import com.paidora.modules.lib.ISharedRefsSrv;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PluginInstanceManager extends ModulesInstanceManagerBase<IPlugin> {
    private final PluginParamsSrv pluginParamsSrv;
    private final ISharedRefsSrv sharedRefsSrv;

    protected PluginInstanceManager(PluginsFactorySrv modulesFactory,
                                    PluginParamsSrv pluginParamsSrv,
                                    ISharedRefsSrv sharedRefsSrv) {
        super(modulesFactory);
        this.pluginParamsSrv = pluginParamsSrv;
        this.sharedRefsSrv = sharedRefsSrv;
    }

    @Override
    protected List<Object> getBeansForModuleContext(Long id) {
        return List.of(sharedRefsSrv);
    }

    @Override
    protected void initializeModuleInstance(ModuleInstanceContainer<IPlugin> moduleInstance) throws Exception {
        var module = moduleInstance.getModule();
        module.start(pluginParamsSrv.getPaymentSystemParams(moduleInstance.getId()));
    }

    @Override
    protected void destroyModuleInstance(ModuleInstanceContainer<IPlugin> moduleInstance) throws Exception {
        var module = moduleInstance.getModule();
        module.stopAndDestroy();
    }
}
