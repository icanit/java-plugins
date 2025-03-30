package com.paidora.framework.spring;

import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@Getter
public class SpringContextConfig {
    private final String contextName;
    private Object[] beans;
    private Class<?>[] configClass;
    private ApplicationContext parentContext;
    private ConfigurableEnvironment environment;
    private ClassLoader classLoader;

    public SpringContextConfig(String contextName) {
        this.contextName = contextName;
    }

    public SpringContextConfig withBeans(Object... beans) {
        this.beans = beans;
        return this;
    }

    public SpringContextConfig withConfigClass(Class<?>... configClass) {
        this.configClass = configClass;
        return this;
    }

    public SpringContextConfig withParentContext(ApplicationContext rootContext) {
        this.parentContext = rootContext;
        return this;
    }

    public SpringContextConfig withEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;
        return this;
    }

    public SpringContextConfig withClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }
}
