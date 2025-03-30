package com.paidora.framework.modules.service.properties;

public interface IModulesRepositoryProperties {

    com.paidora.framework.modules.repository.RepositoryType getRepositoryType();

    String getRepositoryUrl();

    String getRepositoryName();

    String getRepositoryLogin();

    String getRepositoryPassword();

    String getGroupId();
}