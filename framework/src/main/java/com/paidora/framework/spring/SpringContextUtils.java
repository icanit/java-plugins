package com.paidora.framework.spring;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.support.AbstractApplicationContext;

public class SpringContextUtils {

    private SpringContextUtils() {
    }


    public static AnnotationConfigApplicationContext createApplicationContext(SpringContextConfig springContextConfig) {
        var context = new AnnotationConfigApplicationContext();
        return configureSpringContext(context, springContextConfig, true);
    }

    public static <TContext extends AbstractApplicationContext & AnnotationConfigRegistry> TContext configureSpringContext(TContext context,
                                                                                                                           SpringContextConfig springContextConfig, boolean refresh) {
        context.setDisplayName(springContextConfig.getContextName());
        var configClass = springContextConfig.getConfigClass();
        if (configClass != null && configClass.length > 0) {
            context.register(configClass);
        }
        if (springContextConfig.getClassLoader() != null) {
            context.setClassLoader(springContextConfig.getClassLoader());
        }
        var rootParentContext = springContextConfig.getParentContext();
        var beans = springContextConfig.getBeans();
        if (beans != null) {
            var beanFactory = new DefaultListableBeanFactory();
            // creating all beans as @Primary for this context in order for them to be chosen in cases
            // where there are several beans in resulting context
            for (var bean : beans) {
                var beanName = bean.getClass().getCanonicalName();
                var beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(bean.getClass())
                        .setPrimary(true)
                        .getBeanDefinition();
                beanFactory.registerBeanDefinition(beanName, beanDefinition);
                beanFactory.registerSingleton(beanName, bean);
            }
            var parentContext = new AnnotationConfigApplicationContext(beanFactory);
            parentContext.setDisplayName(springContextConfig.getContextName() + "_SingletonServiceProxyContext");
            if (rootParentContext != null) {
                parentContext.setParent(rootParentContext);
            }
            parentContext.refresh();
            rootParentContext = parentContext;
        }
        if (rootParentContext != null) {
            context.setParent(rootParentContext);
        }
        if (springContextConfig.getEnvironment() != null) {
            context.setEnvironment(springContextConfig.getEnvironment());
        }
        if (refresh) {
            context.refresh();
        }
        return context;
    }
}
