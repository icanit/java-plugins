package com.paidora.framework.modules.service;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.paidora.framework.modules.exceptions.ModuleInstanceException;
import com.paidora.framework.spring.SpringContextConfig;
import com.paidora.framework.spring.SpringContextUtils;
import com.paidora.framework.utils.CloseableReentrantLock;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public abstract class ModulesInstanceManagerBase<TModule> {
    private final ModulesFactorySrvBase modulesFactory;
    private final Map<Long, ModuleInstanceContainer<TModule>> moduleInstances = new ConcurrentHashMap<>();
    private final Map<Long, CloseableReentrantLock> reentrantLocks = new ConcurrentHashMap<>();

    protected ModulesInstanceManagerBase(ModulesFactorySrvBase modulesFactory) {
        this.modulesFactory = modulesFactory;
        modulesFactory.registerModuleUpdateListener(this::updateModulesWithName);
    }

    @Synchronized("reentrantLocks")
    private CloseableReentrantLock getModuleInstanceLock(Long instanceId) {
        return reentrantLocks.computeIfAbsent(instanceId, n -> new CloseableReentrantLock());
    }

    protected CloseableReentrantLock openModuleInstanceLock(Long instanceId) {
        var lock = getModuleInstanceLock(instanceId);
        return lock != null ? lock.open() : null;
    }

    private void updateModulesWithName(String moduleName) {
        var modulesToRecreate = moduleInstances.values().stream()
                .filter(m -> m.getModuleName().equals(moduleName))
                .collect(Collectors.toList());
        for (var module : modulesToRecreate) {
            try {
                reloadModule(module.getId());
            } catch (ModuleInstanceException e) {
                log.warn("Failed to reload module :" + module);
            }
        }
    }

    public void reloadModule(Long id) throws ModuleInstanceException {
        try (var ignored = openModuleInstanceLock(id)) {
            var module = moduleInstances.get(id);
            if (module != null) {
                try {
                    removeModuleInstance(id);
                } catch (Throwable e) {
                    throw new ModuleInstanceException("Failed to cleanup plugin instance for " + id + "/" + module.getModuleName(), e);
                }
                createModuleInstance(id, module.getModuleName());
            } else {
                throw new ModuleInstanceException("There is no module instance created for module with id: " + id);
            }
        }
    }


    public ModuleInstanceContainer<TModule> createModuleInstance(@NonNull Long id, @NonNull String moduleName) throws ModuleInstanceException {
        try (var ignored = openModuleInstanceLock(id)) {
            var module = moduleInstances.get(id);
            if (module != null) {
                return module;
            }
            var moduleLoader = modulesFactory.getModule(moduleName);
            if (moduleLoader == null) {
                throw new ModuleInstanceException("Module not found with name: " + moduleName + " for id: " + id);
            }
            var beansToInject = getBeansForModuleContext(id);


            var moduleClass = moduleLoader.getModuleClass();
            var contextConfig = new SpringContextConfig("ModuleContext_" + moduleName + "_" + id)
                    .withClassLoader(moduleLoader.getClassLoader())
                    .withBeans(beansToInject.toArray());
            if (moduleLoader.isSpringRegistrable()) {
                contextConfig.withConfigClass(moduleClass);
            }

            var moduleContext = SpringContextUtils.createApplicationContext(contextConfig);
            moduleContext.start();
            TModule moduleInstance;
            try {
                @SuppressWarnings("unchecked")
                var existingModuleInstance = (TModule) moduleContext.getBean(moduleClass);
                moduleInstance = existingModuleInstance;
            } catch (BeansException e) {
                moduleInstance = null;
            }
            if (moduleInstance == null) {
                @SuppressWarnings("unchecked")
                var createdModuleInstance = (TModule) moduleContext.getAutowireCapableBeanFactory().createBean(moduleClass);
                moduleInstance = createdModuleInstance;
            }
            var newModuleInstanceContainer = ModuleInstanceContainer.<TModule>builder()
                    .id(id)
                    .moduleName(moduleName)
                    .module(moduleInstance)
                    .loaderContainer(moduleLoader)
                    .moduleContext(moduleContext)
                    .build();
            try {
                initializeModuleInstance(newModuleInstanceContainer);
            } catch (Throwable e) {
                try {
                    destroyModuleInstance(newModuleInstanceContainer);
                } catch (Throwable ex) {
                    throw new ModuleInstanceException("Failed to cleanup from failed module instance for " + id + "/" + moduleName, ex);
                }
                moduleContext.stop();
                moduleContext.close();
                TypeFactory.defaultInstance().clearCache();
                throw new ModuleInstanceException("Failed to create module instance for " + id + "/" + moduleName, e);
            }

            moduleInstances.put(id, newModuleInstanceContainer);
            return newModuleInstanceContainer;
        }
    }

    public ModuleInstanceContainer<TModule> getModuleInstance(Long id) {
        try (var ignored = openModuleInstanceLock(id)) {
            return moduleInstances.get(id);
        }
    }

    public void removeAllModuleInstances() {
        for (var instanceId : getInstanceIds()) {
            try {
                removeModuleInstance(instanceId);
            } catch (Exception e) {
                log.error("Error removing module instance with id: " + instanceId, e);
            }
        }
    }

    public void removeModuleInstance(Long id) throws ModuleInstanceException {
        try (var ignored = openModuleInstanceLock(id)) {
            var instance = moduleInstances.remove(id);
            if (instance != null) {
                destroyModuleInstance(instance);
                instance.moduleContext.stop();
                instance.moduleContext.close();
                TypeFactory.defaultInstance().clearCache();
            }
        } catch (Throwable e) {
            throw new ModuleInstanceException("Failed to cleanup module instance for " + id, e);
        }
    }

    public Collection<Long> getInstanceIds() {
        return moduleInstances.keySet();
    }

    protected abstract List<Object> getBeansForModuleContext(Long id);

    protected abstract void initializeModuleInstance(ModuleInstanceContainer<TModule> module) throws Exception;

    protected abstract void destroyModuleInstance(ModuleInstanceContainer<TModule> module) throws Exception;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleInstanceContainer<TModule> {
        private Long id;
        private String moduleName;
        private TModule module;
        private ModulesFactorySrvBase.ModuleLoaderContainer loaderContainer;
        private GenericApplicationContext moduleContext;

        @Override
        public String toString() {
            return "ModuleInstanceContainer{" +
                    "id=" + id +
                    ", moduleName='" + moduleName + '\'' +
                    ", module=" + module +
                    ", loaderContainer=" + loaderContainer +
                    '}';
        }
    }
}
