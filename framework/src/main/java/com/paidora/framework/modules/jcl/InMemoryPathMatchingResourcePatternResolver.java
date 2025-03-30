package com.paidora.framework.modules.jcl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
public class InMemoryPathMatchingResourcePatternResolver extends PathMatchingResourcePatternResolver {

    public InMemoryPathMatchingResourcePatternResolver(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    protected Resource[] findPathMatchingResources(String locationPattern) throws IOException {
        String rootDirPath = determineRootDir(locationPattern);
        String subPattern = locationPattern.substring(rootDirPath.length());
        Resource[] rootDirResources = getResources(rootDirPath);
        Set<Resource> result = new LinkedHashSet<>(16);
        for (Resource rootDirResource : rootDirResources) {
            rootDirResource = resolveRootDirResource(rootDirResource);
            URL rootDirUrl = rootDirResource.getURL();
            if (ResourceUtils.isJarURL(rootDirUrl) || isJarResource(rootDirResource)) {
                result.addAll(doFindPathMatchingJarResources(rootDirResource, rootDirUrl, subPattern));
            } else if ("x-mem-cache".equals(rootDirUrl.getProtocol())) {
                result.addAll(doFindPathMatchingInMemoryJarResources(rootDirResource, rootDirUrl, subPattern));
            } else {
                result.addAll(doFindPathMatchingFileResources(rootDirResource, subPattern));
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Resolved location pattern [" + locationPattern + "] to resources " + result);
        }
        return result.toArray(new Resource[0]);
    }

    private Collection<? extends Resource> doFindPathMatchingInMemoryJarResources(Resource rootDirResource, URL rootDirURL, String subPattern) throws IOException {
        var con = rootDirURL.openConnection();

        if (con instanceof InMemoryJarClassLoader.InMemoryJarURLConnection) {
            var jarCon = (InMemoryJarClassLoader.InMemoryJarURLConnection) con;
            var rootEntryPath = jarCon.getEntry();
            var result = new LinkedHashSet<Resource>(8);
            for (var entryPath : jarCon.getAllEntries()) {
                if (entryPath.startsWith(rootEntryPath)) {
                    var relativePath = entryPath.substring(rootEntryPath.length());
                    if (this.getPathMatcher().match(subPattern, relativePath)) {
                        result.add(rootDirResource.createRelative(relativePath));
                    }
                }
            }
            return result;
        } else {
            return Collections.emptySet();
        }
    }
}
