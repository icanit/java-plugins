package com.paidora.app.controllers;

import com.paidora.app.models.DataResponse;
import com.paidora.app.models.ModuleParams;
import com.paidora.app.models.ModuleState;
import com.paidora.app.services.ActionSrv;
import com.paidora.app.services.PluginProperties;
import com.paidora.app.services.plugin.PluginInstanceManager;
import com.paidora.app.services.plugin.PluginsManager;
import com.paidora.app.services.plugin.PluginsUpdateManager;
import com.paidora.framework.modules.models.ModuleUpdateInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/plugin", produces = "application/json")
public class PluginApiController {
    //region Injections
    private final PluginsUpdateManager updateManager;
    private final PluginInstanceManager instanceManager;
    private final PluginsManager pluginsManager;
    private final PluginProperties pluginProperties;
    private final ActionSrv actionSrv;

    //endregion


    //region Modules

    @GetMapping(value = "/modules/modules-update-info")
    public DataResponse<ModuleUpdateInfo> getModuleUpdatesInfo() {
        var ui = updateManager.getModuleUpdatesInfo();
        var u = ui.getModules().stream().filter(mu -> mu.getModuleName().equals(pluginProperties.getName())).findFirst().orElse(null);
        return DataResponse.success(u);
    }

    @GetMapping(value = "/modules/instance-info")
    public DataResponse<ModuleState> getModuleInstanceInfo() {
        var instance = instanceManager.getModuleInstance(1L);
        if (instance == null) {
            return DataResponse.error("No instance");
        }
        return DataResponse.success(ModuleState.builder()
                .moduleAlias(instance.getModuleName())
                .moduleParams(ModuleParams.builder()
                        .pluginParams(pluginProperties.getParams())
                        .build())
                .started(true)
                .version(instance.getLoaderContainer().getVersion())
                .build());
    }

    @GetMapping(value = "/modules/update-module")
    public DataResponse<Boolean> getUpdateModule(@RequestParam String moduleName, @RequestParam String versionTo) {
        return DataResponse.success(updateManager.updateModule(moduleName, versionTo));
    }

    @GetMapping(value = "/modules/restart-module")
    public DataResponse<Boolean> restartModule() {
        return DataResponse.success(pluginsManager.restartPlugin(1L));
    }
    //endregion

    //region Actions
    @PostMapping(value = "/action/doSomething")
    public DataResponse<String> doSomething(@RequestBody() String request) throws Exception {
        return DataResponse.success(actionSrv.doSomething(request));
    }
    //endregion
}
