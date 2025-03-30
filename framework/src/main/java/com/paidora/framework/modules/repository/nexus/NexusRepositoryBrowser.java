package com.paidora.framework.modules.repository.nexus;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import com.paidora.framework.modules.repository.DownloadedJar;
import com.paidora.framework.modules.repository.IRepositoryBrowser;
import com.paidora.framework.modules.service.properties.IModulesRepositoryProperties;
import org.apache.maven.artifact.repository.metadata.Metadata;

import java.util.List;

public class NexusRepositoryBrowser implements IRepositoryBrowser {
    private final NexusApiClient nexusApiClient;


    public NexusRepositoryBrowser(IModulesRepositoryProperties repositoryProperties) {
        this.nexusApiClient = new NexusApiClient(repositoryProperties);
    }

    @Override
    public List<String> getAvailableArtifactIds() throws UnexpectedBehaviourException {
        return nexusApiClient.getAvailableArtifactIds();
    }

    @Override
    public Metadata getArtifactMetadata(String artifactId) throws UnexpectedBehaviourException {
        return nexusApiClient.getArtifactMetadata(artifactId);
    }

    @Override
    public DownloadedJar downloadJar(String artifactId, String version) throws UnexpectedBehaviourException {
        return nexusApiClient.getArtifactBinary(artifactId, version);
    }
}
