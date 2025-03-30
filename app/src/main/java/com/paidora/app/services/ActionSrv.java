package com.paidora.app.services;

import com.paidora.app.services.plugin.PluginsManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActionSrv {

    private final PluginsManager pluginsManager;

    public String doSomething(String request) {
        var plugin = pluginsManager.getModule(1L);
        return plugin.doSomething(request);
    }
}
