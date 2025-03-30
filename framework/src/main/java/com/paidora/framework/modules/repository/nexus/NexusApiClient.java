package com.paidora.framework.modules.repository.nexus;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import com.paidora.framework.http.client.ApiClientBase;
import com.paidora.framework.http.client.core.ApiClientBodyType;
import com.paidora.framework.http.client.core.ApiClientHttpRequest;
import com.paidora.framework.http.client.core.auth.BasicApiClientHttpAuth;
import com.paidora.framework.modules.repository.DownloadedJar;
import com.paidora.framework.modules.service.properties.IModulesRepositoryProperties;
import com.paidora.framework.utils.uri.Uri;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.stream.Collectors;

public class NexusApiClient extends ApiClientBase {
    private final String repositoryName;
    private final String groupId;
    private final String groupPath;
    private final BasicApiClientHttpAuth auth;

    public NexusApiClient(IModulesRepositoryProperties repositoryProperties) {
        super(repositoryProperties.getRepositoryUrl(), 90L);
        this.repositoryName = repositoryProperties.getRepositoryName();
        this.groupId = repositoryProperties.getGroupId();
        this.groupPath = groupId.replace('.', '/');
        this.auth = BasicApiClientHttpAuth.of(repositoryProperties.getRepositoryLogin(), repositoryProperties.getRepositoryPassword());
    }

    public List<String> getAvailableArtifactIds() throws UnexpectedBehaviourException {
        var url = Uri.parseUri(getUrl())
                .setPath("service/rest/repository/browse")
                .setRelativePath(repositoryName)
                .setRelativePath(groupPath)
                .setRelativePath("/");
        var request = ApiClientHttpRequest.get(url.toString(), ApiClientBodyType.TEXT, String.class);
        request.setApiClientHttpAuth(auth);
        var html = makeApiRequest(request);
        Document document = Jsoup.parse(html);
        var elements = document.select("body > table > tbody > tr > td:nth-child(1) > a");
        return elements.eachText().stream().filter(t -> !t.equals("Parent Directory")).collect(Collectors.toList());
    }

    public Metadata getArtifactMetadata(String artifactId) throws UnexpectedBehaviourException {
        var url = Uri.parseUri(getUrl())
                .setPath("repository")
                .setRelativePath(repositoryName)
                .setRelativePath(groupPath)
                .setRelativePath(artifactId)
                .setRelativePath("maven-metadata.xml");
        var request = ApiClientHttpRequest.get(url.toString(), ApiClientBodyType.XML, Metadata.class);
        request.setApiClientHttpAuth(auth);
        return makeApiRequest(request);
    }

    public DownloadedJar getArtifactBinary(String artifactId, String version) throws UnexpectedBehaviourException {
        var jarFileName = artifactId + "-" + version + ".jar";
        var url = Uri.parseUri(getUrl())
                .setPath("repository")
                .setRelativePath(repositoryName)
                .setRelativePath(groupPath)
                .setRelativePath(artifactId)
                .setRelativePath(version)
                .setRelativePath(jarFileName);
        var request = ApiClientHttpRequest.get(url.toString(), ApiClientBodyType.BLOB, byte[].class);
        request.setApiClientHttpAuth(auth);
        byte[] jarBytes = makeApiRequest(request);
        return DownloadedJar.builder()
                .fileName(jarFileName)
                .jarBytes(jarBytes)
                .build();
    }
}
