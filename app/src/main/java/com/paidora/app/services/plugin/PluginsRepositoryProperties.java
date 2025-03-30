package com.paidora.app.services.plugin;

import com.paidora.framework.modules.repository.RepositoryType;
import com.paidora.framework.modules.service.properties.IModulesRepositoryProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PluginsRepositoryProperties implements IModulesRepositoryProperties {
    @Value("${plugins.repository.type}")
    private RepositoryType repositoryType;
    @Value("${plugins.repository.url}")
    private String repositoryUrl;
    @Value("${plugins.repository.name}")
    private String repositoryName;
    @Value("${plugins.repository.login}")
    private String repositoryLogin;
    @Value("${plugins.repository.password}")
    private String repositoryPassword;
    @Value("${plugins.repository.groupId}")
    private String groupId;
}
