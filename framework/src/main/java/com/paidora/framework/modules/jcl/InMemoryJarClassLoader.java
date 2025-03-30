package com.paidora.framework.modules.jcl;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


/**
 * Класслоадер из джарника, который хранит его в памяти, таким образом не блокируя операции с файлом на урвоне ОС
 */
@Slf4j
public class InMemoryJarClassLoader extends URLClassLoader {

    private final Map<String, byte[]> cache;
    private final int BUFFER_SIZE = 8192;

    public InMemoryJarClassLoader(File jarFile, ClassLoader parent) throws IOException {
        this(() -> {
            try {
                return new FileInputStream(jarFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }, parent);
    }

    public InMemoryJarClassLoader(byte[] jarBytes, ClassLoader parent) throws IOException {
        this(() -> new ByteArrayInputStream(jarBytes), parent);
    }

    protected InMemoryJarClassLoader(Supplier<InputStream> getInputStream, ClassLoader parent) throws IOException {
        super(new URL[]{}, parent);
        cache = new ConcurrentHashMap<>();
        var cacheURL = new URL("x-mem-cache",
                null,
                -1,
                "/",
                new InMemoryJarURLStreamHandler(cache));
        super.addURL(cacheURL);
        loadJar(getInputStream);
    }

    private void loadJar(Supplier<InputStream> getInputStream) throws IOException {
        String name;
        byte[] b = new byte[BUFFER_SIZE];
        int len = 0;
        try (var fis = getInputStream.get()) {
            try (var jis = new JarInputStream(fis)) {
                JarEntry jarEntry;
                while ((jarEntry = jis.getNextJarEntry()) != null) {
                    name = "/" + jarEntry.getName();

                    if (jarEntry.isDirectory()) {
                        cache.put(name, new byte[]{});
                        continue;
                    }

                    if (cache.containsKey(name)) {
                        log.debug("Class/Resource " + name + " already loaded; ignoring entry...");
                        continue;
                    }

                    try (var out = new ByteArrayOutputStream()) {
                        while ((len = jis.read(b)) > 0) {
                            out.write(b, 0, len);
                        }

                        log.debug("Jar entry = " + name);

                        cache.put(name, out.toByteArray());
                    }
                }
                var manifest = jis.getManifest();
                if (manifest != null) {
                    try (var out = new ByteArrayOutputStream()) {
                        manifest.write(out);
                        cache.put("/META-INF/MANIFEST.MF", out.toByteArray());
                    }
                }
            }
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        var loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            try {
                loadedClass = findClass(name);
            } catch (ClassNotFoundException e) {
                loadedClass = super.loadClass(name, resolve);
            }
        }

        if (resolve) {
            resolveClass(loadedClass);
        }
        return loadedClass;
    }

    @Override
    public URL getResource(String name) {
        Objects.requireNonNull(name);
        var url = findResource(name);
        if (url == null) {
            url = super.getResource(name);
        }
        return url;
    }

    protected class InMemoryJarURLStreamHandler extends URLStreamHandler {

        private final Map<String, byte[]> cache;

        public InMemoryJarURLStreamHandler(Map<String, byte[]> cache) {
            this.cache = cache;
        }

        @Override
        protected URLConnection openConnection(URL url) {
            return new InMemoryJarURLConnection(url, cache);
        }

    }

    protected class InMemoryJarURLConnection extends URLConnection {
        private final Map<String, byte[]> cache;

        public InMemoryJarURLConnection(URL url, Map<String, byte[]> cache) {
            super(url);
            this.cache = cache;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            var fileName = url.getFile();

            byte[] data = this.cache.get(fileName);

            if (data == null) {
                throw new FileNotFoundException(fileName);
            }

            return new ByteArrayInputStream(data);
        }

        public Set<String> getAllEntries() {
            return cache.keySet();
        }

        public String getEntry() {
            return url.getFile();
        }
    }
}