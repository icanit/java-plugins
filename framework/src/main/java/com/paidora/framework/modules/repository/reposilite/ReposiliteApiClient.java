package com.paidora.framework.modules.repository.reposilite;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import com.paidora.framework.http.client.ApiClientBase;
import com.paidora.framework.http.client.core.ApiClientBodyType;
import com.paidora.framework.http.client.core.ApiClientHttpRequest;
import com.paidora.framework.modules.repository.DownloadedJar;
import com.paidora.framework.modules.service.properties.IModulesRepositoryProperties;
import com.paidora.framework.utils.uri.Uri;
import org.apache.maven.artifact.repository.metadata.Metadata;

public class ReposiliteApiClient extends ApiClientBase {
    private final String repositoryPath;

    public ReposiliteApiClient(IModulesRepositoryProperties repositoryProperties) {
        super(repositoryProperties.getRepositoryUrl(), 90L);
        var groupPath = repositoryProperties.getGroupId().replace('.', '/');
        this.repositoryPath = repositoryProperties.getRepositoryName() + '/' + groupPath;
    }

    public ReposiliteFile listModulesInRepository() throws UnexpectedBehaviourException {
        var url = Uri.parseUri(getUrl())
                .setRelativePath("api/maven/details/")
                .setRelativePath(repositoryPath);
        var request = ApiClientHttpRequest.get(url.toString(), ApiClientBodyType.JSON, ReposiliteFile.class);
        return makeApiRequest(request);
    }

    public Metadata getArtifactMetadata(String artifactId) throws UnexpectedBehaviourException {
        var url = Uri.parseUri(getUrl())
                .setRelativePath(repositoryPath)
                .setRelativePath(artifactId)
                .setRelativePath("maven-metadata.xml");
        var request = ApiClientHttpRequest.get(url.toString(), ApiClientBodyType.XML, Metadata.class);
        return makeApiRequest(request);
    }

    public DownloadedJar getArtifactBinary(String artifactId, String version) throws UnexpectedBehaviourException {
        var jarFileName = artifactId + "-" + version + ".jar";
        var url = Uri.parseUri(getUrl())
                .setRelativePath(repositoryPath)
                .setRelativePath(artifactId)
                .setRelativePath(version)
                .setRelativePath(jarFileName);
        var request = ApiClientHttpRequest.get(url.toString(), ApiClientBodyType.BLOB, byte[].class);
        byte[] jarBytes = makeApiRequest(request);
        return DownloadedJar.builder()
                .fileName(jarFileName)
                .jarBytes(jarBytes)
                .build();
    }
}
