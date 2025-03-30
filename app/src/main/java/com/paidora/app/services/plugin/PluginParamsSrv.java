package com.paidora.app.services.plugin;

import com.paidora.app.services.PluginProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PluginParamsSrv {
    private final PluginProperties pluginProperties;

    public Map<String, String> getPaymentSystemParams(Long paymentSystemId) {
        return pluginProperties.getParams();
    }
}
