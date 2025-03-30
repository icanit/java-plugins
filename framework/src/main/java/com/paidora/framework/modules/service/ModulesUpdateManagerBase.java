package com.paidora.framework.modules.service;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import com.paidora.framework.modules.models.ModuleUpdateInfo;
import com.paidora.framework.modules.models.ModuleUpdatesInfo;
import com.paidora.framework.modules.repository.DownloadedJar;
import com.paidora.framework.modules.repository.IRepositoryBrowser;
import com.paidora.framework.modules.repository.nexus.NexusRepositoryBrowser;
import com.paidora.framework.modules.repository.reposilite.ReposiliteRepositoryBrowser;
import com.paidora.framework.modules.service.properties.IModulesRepositoryProperties;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.artifact.repository.metadata.Metadata;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ModulesUpdateManagerBase {
    private final String jarPrefix;
    private final ModulesFactorySrvBase factorySrv;
    private Map<String, Metadata> modulesInRepository = new HashMap<>();
    private IRepositoryBrowser repositoryBrowser;

    public ModulesUpdateManagerBase(String jarPrefix,
                                    IModulesRepositoryProperties repositoryProperties,
                                    ModulesFactorySrvBase factorySrv) {
        this.jarPrefix = jarPrefix;
        this.factorySrv = factorySrv;

        switch (repositoryProperties.getRepositoryType()) {
            case REPOSILITE:
                repositoryBrowser = new ReposiliteRepositoryBrowser(repositoryProperties);
                break;
            case NEXUS:
                repositoryBrowser = new NexusRepositoryBrowser(repositoryProperties);
                break;
            case NONE:
            default:
                break;
        }
    }

    public ModuleUpdatesInfo getModuleUpdatesInfo() {
        log.debug("Checking for modules updates...");
        var moduleNamesLocal = this.factorySrv.getLoadedModuleNames();
        var moduleNamesRepo = modulesInRepository.keySet();
        var allModuleNames = new HashSet<String>();
        allModuleNames.addAll(moduleNamesLocal);
        allModuleNames.addAll(moduleNamesRepo);
        var modulesInfo = new ArrayList<ModuleUpdateInfo>();
        for (var moduleName : allModuleNames) {
            var localModule = this.factorySrv.getModule(moduleName);
            var repoModule = this.modulesInRepository.get(moduleName);
            modulesInfo.add(ModuleUpdateInfo.builder()
                    .moduleName(moduleName)
                    .localVersion(localModule != null ? localModule.getVersion() : null)
                    .repoLastVersion(repoModule != null ? repoModule.getVersioning().getLatest() : null)
                    .repoVersions(repoModule != null ? repoModule.getVersioning().getVersions() : null)
                    .build());
        }
        return ModuleUpdatesInfo.builder()
                .modules(modulesInfo.stream().sorted(Comparator.comparing(ModuleUpdateInfo::getModuleName)).collect(Collectors.toList()))
                .build();
    }

    protected boolean loadModulesFromRepository() {
        if (repositoryBrowser == null) {
            return false;
        }
        log.debug("Loading modules from repository...");
        var modulesInRepositoryNew = new HashMap<String, Metadata>();
        List<String> modules;
        try {
            modules = repositoryBrowser.getAvailableArtifactIds();
        } catch (UnexpectedBehaviourException e) {
            log.warn("Can't load artifact list from repository", e);
            return false;
        }
        if (modules != null) {
            for (var artifactName : modules) {
                if (!artifactName.startsWith(jarPrefix)) {
                    continue;
                }
                Metadata artifactMetadata;
                try {
                    artifactMetadata = repositoryBrowser.getArtifactMetadata(artifactName);
                } catch (UnexpectedBehaviourException e) {
                    log.warn("Can't load artifact metadata for: " + artifactName, e);
                    continue;
                }
                log.debug("Loaded module metadata: " + modulesInRepositoryNew.size());
                var moduleName = artifactName.substring(jarPrefix.length());
                modulesInRepositoryNew.put(moduleName, artifactMetadata);
            }
        }
        this.modulesInRepository = modulesInRepositoryNew;
        log.debug("Loaded repository modules: " + modulesInRepositoryNew.size());
        return true;
    }

    @Synchronized
    public boolean updateModule(String moduleName, String versionToUpdate) {
        log.info("Updating module " + moduleName + " to version " + versionToUpdate);
        var repoModule = modulesInRepository.get(moduleName);
        var versionAvailable = repoModule.getVersioning().getVersions().contains(versionToUpdate);
        if (!versionAvailable) {
            log.info("Version " + versionToUpdate + " not available for module " + moduleName);
            return false;
        }
        DownloadedJar jarBlob;
        try {
            jarBlob = repositoryBrowser.downloadJar(repoModule.getArtifactId(), versionToUpdate);
        } catch (UnexpectedBehaviourException e) {
            log.info("Can't get jar from repository for module " + moduleName + " to version " + versionToUpdate, e);
            return false;
        }
        return factorySrv.tryToUpdateModuleJar(jarBlob.getFileName(), jarBlob.getJarBytes(), moduleName, versionToUpdate);
    }

    public boolean forceModulesRepoUpdate() {
        return loadModulesFromRepository();
    }
}
