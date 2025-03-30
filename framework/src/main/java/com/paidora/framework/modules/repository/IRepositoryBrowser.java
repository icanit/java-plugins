package com.paidora.framework.modules.repository;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import org.apache.maven.artifact.repository.metadata.Metadata;

import java.util.List;

public interface IRepositoryBrowser {
    List<String> getAvailableArtifactIds() throws UnexpectedBehaviourException;

    Metadata getArtifactMetadata(String artifactId) throws UnexpectedBehaviourException;

    DownloadedJar downloadJar(String artifactId, String version) throws UnexpectedBehaviourException;
}
