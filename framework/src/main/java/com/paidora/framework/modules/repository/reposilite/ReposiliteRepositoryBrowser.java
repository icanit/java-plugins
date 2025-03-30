package com.paidora.framework.modules.repository.reposilite;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import com.paidora.framework.modules.repository.DownloadedJar;
import com.paidora.framework.modules.repository.IRepositoryBrowser;
import com.paidora.framework.modules.service.properties.IModulesRepositoryProperties;
import org.apache.maven.artifact.repository.metadata.Metadata;

import java.util.List;
import java.util.stream.Collectors;

public class ReposiliteRepositoryBrowser implements IRepositoryBrowser {

    private final ReposiliteApiClient repositoryApiClient;


    public ReposiliteRepositoryBrowser(IModulesRepositoryProperties repositoryProperties) {
        this.repositoryApiClient = new ReposiliteApiClient(repositoryProperties);
    }

    @Override
    public List<String> getAvailableArtifactIds() throws UnexpectedBehaviourException {
        var modules = repositoryApiClient.listModulesInRepository();

        if (modules == null) {
            return null;
        }
        return modules.getFiles().stream()
                .filter(m -> "DIRECTORY".equals(m.getType()))
                .map(ReposiliteFile::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Metadata getArtifactMetadata(String artifactId) throws UnexpectedBehaviourException {
        return repositoryApiClient.getArtifactMetadata(artifactId);
    }

    @Override
    public DownloadedJar downloadJar(String artifactId, String version) throws UnexpectedBehaviourException {
        return repositoryApiClient.getArtifactBinary(artifactId, version);
    }
}
