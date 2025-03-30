package com.paidora.framework.modules.service;

import com.jcabi.manifests.Manifests;
import com.jcabi.manifests.StreamsMfs;
import com.paidora.framework.modules.Module;
import com.paidora.framework.modules.exceptions.ModuleLoaderException;
import com.paidora.framework.modules.jcl.InMemoryJarClassLoader;
import com.paidora.framework.modules.jcl.InMemoryPathMatchingResourcePatternResolver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public abstract class ModulesFactorySrvBase {
    private final Map<String, ModuleLoaderContainer> loadedModules = new HashMap<>();
    private final String modulesRoot;
    private final ModulesStorageManagerBase storageManager;
    private final List<Consumer<String>> moduleUpdateListeners = new ArrayList<>();

    protected ModulesFactorySrvBase(String modulesRoot, ModulesStorageManagerBase storageManager) {
        this.modulesRoot = modulesRoot;
        this.storageManager = storageManager;
    }

    protected void loadModulesFromDir() {
        var files = storageManager.getAvailableJars();
        for (var file : files) {
            ModuleLoaderContainer module = null;
            try {
                module = loadModuleFromJar(file);
            } catch (ModuleLoaderException e) {
                log.warn("Can't load jar: " + file, e);
            }
            if (module != null) {
                log.info("Loaded module: " + module);
                loadedModules.put(module.getName(), module);
            }
        }
    }

    private ModuleLoaderContainer loadModuleFromJar(File file) throws ModuleLoaderException {
        try {
            var classLoader = new InMemoryJarClassLoader(file, this.getClass().getClassLoader());
            var scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Module.class));
            scanner.setResourceLoader(new InMemoryPathMatchingResourcePatternResolver(classLoader));
            var beans = scanner.findCandidateComponents(modulesRoot);
            var bean = beans.stream().findFirst();
            if (bean.isEmpty()) {
                throw new ModuleLoaderException("No module found in jar: " + file);
            }
            var className = bean.get().getBeanClassName();
            var moduleClass = classLoader.loadClass(className);
            var moduleDefinition = moduleClass.getAnnotation(Module.class);
            var isSpringComponent = moduleClass.isAnnotationPresent(Component.class);
            var resource = classLoader.getResource("META-INF/MANIFEST.MF");
            String version = null;

            try {
                var manifests = new Manifests();
                manifests.append(new StreamsMfs(Objects.requireNonNull(resource).openStream()));
                if (manifests.containsKey("Bundle-Build")) {
                    version = manifests.get("Bundle-Build");
                }
            } catch (NullPointerException | IOException ignored) {
            }

            if (version == null) {
                throw new ModuleLoaderException("Module version is unknown");
            }
            return ModuleLoaderContainer.builder()
                    .name(moduleDefinition.value())
                    .moduleClass(moduleClass)
                    .springRegistrable(isSpringComponent)
                    .classLoader(classLoader)
                    .version(version)
                    .jarFile(file)
                    .build();
        } catch (Throwable e) {
            throw new ModuleLoaderException("loadModuleFromJar error", e);
        }
    }

    public ModuleLoaderContainer getModule(String moduleAlias) {
        return loadedModules.get(moduleAlias);
    }

    public Set<String> getLoadedModuleNames() {
        return loadedModules.keySet();
    }

    public boolean tryToUpdateModuleJar(String fileName, byte[] jarBlob, String expectedModuleName, String expectedVersion) {
        File temporaryJarFile = null;
        try {
            temporaryJarFile = File.createTempFile(fileName + "-", ".jar");
            temporaryJarFile.deleteOnExit();
            try (var fos = new FileOutputStream(temporaryJarFile)) {
                fos.write(jarBlob);
            }
            ModuleLoaderContainer downloadedModule;
            try {
                downloadedModule = loadModuleFromJar(temporaryJarFile);
                downloadedModule.getClassLoader().close();
            } catch (ModuleLoaderException e) {
                log.warn("Can't load new jar: " + temporaryJarFile, e);
                return false;
            }
            log.info("Loaded module from new jar: " + downloadedModule);

            if (expectedModuleName.equals(downloadedModule.getName()) && expectedVersion.equals(downloadedModule.getVersion())) {
                // Module name and version are as expected, proceed with replacing existing module
                var existingModule = loadedModules.get(expectedModuleName);
                File newJarFile;
                if (existingModule != null) {
                    existingModule.getClassLoader().close();
                    log.info("Replaced module: " + existingModule.getName() + " with new version: " + downloadedModule.getVersion());
                    newJarFile = storageManager.replaceExistingJar(fileName, downloadedModule.getJarFile(), existingModule.getJarFile());
                } else {
                    log.info("Added new module: " + downloadedModule.getName() + " with version: " + downloadedModule.getVersion());
                    newJarFile = storageManager.addNewJar(fileName, downloadedModule.getJarFile());
                }
                var newModule = loadModuleFromJar(newJarFile);
                loadedModules.put(newModule.getName(), newModule);
                notifyAboutModuleUpdate(newModule.getName());
            } else {
                log.warn("The uploaded module's name " + downloadedModule.getName() + " and version " + downloadedModule.getVersion() + " do not match expected values");
                return false;
            }
        } catch (Exception e) {
            log.warn("Module jar update error", e);
            return false;
        } finally {
            if (temporaryJarFile != null && temporaryJarFile.exists()) {
                temporaryJarFile.delete();
            }
        }
        return true;
    }

    private void notifyAboutModuleUpdate(String name) {
        for (var listener : moduleUpdateListeners) {
            try {
                listener.accept(name);
            } catch (Throwable e) {
                log.warn("Error in module update listener", e);
            }
        }
    }

    public void registerModuleUpdateListener(Consumer<String> updateListener) {
        this.moduleUpdateListeners.add(updateListener);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleLoaderContainer {
        private String name;
        private Class<?> moduleClass;
        private boolean springRegistrable;
        private URLClassLoader classLoader;
        private String version;
        private File jarFile;

        @Override
        public String toString() {
            return "ModuleLoaderContainer{" +
                    "name='" + name + '\'' +
                    ", moduleClass=" + moduleClass +
                    ", version='" + version + '\'' +
                    ", jarFile='" + jarFile + '\'' +
                    '}';
        }
    }
}