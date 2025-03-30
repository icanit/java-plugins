package com.paidora.framework.modules.service;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class ModulesStorageManagerBase {
    private final String modulesDir;

    protected ModulesStorageManagerBase(String modulesDir) {
        this.modulesDir = modulesDir;
    }

    public List<File> getAvailableJars() {
        var dir = new File(modulesDir);
        var files = dir.listFiles((d, name) -> name.endsWith(".jar"));
        return files != null ? List.of(files) : new ArrayList<>();
    }

    public File replaceExistingJar(String newJarFileName, File newJarFile, File oldJarFile) throws UnexpectedBehaviourException {
        log.info("Replacing old jar: " + oldJarFile + " with new jar from file: " + newJarFile + " named " + newJarFileName);
        File targetJar;

        try {
            Files.delete(oldJarFile.toPath());
        } catch (IOException e) {
            throw new UnexpectedBehaviourException("Can't delete old jar: " + oldJarFile, e);
        }
        try {
            targetJar = Files.copy(newJarFile.toPath(), Path.of(modulesDir, newJarFileName), StandardCopyOption.REPLACE_EXISTING).toFile();
            return targetJar;
        } catch (Exception e) {
            throw new UnexpectedBehaviourException("Error ocurred while replacing new jar", e);
        }
    }

    public File addNewJar(String newJarFileName, File newJarFile) throws UnexpectedBehaviourException {
        log.info("Creating new jar from file: " + newJarFile + " named " + newJarFileName);
        try {
            return Files.copy(newJarFile.toPath(), Path.of(modulesDir, newJarFileName), StandardCopyOption.REPLACE_EXISTING).toFile();
        } catch (IOException e) {
            throw new UnexpectedBehaviourException("Error ocurred while creating new jar", e);
        }
    }
}
