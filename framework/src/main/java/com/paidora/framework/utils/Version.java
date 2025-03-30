package com.paidora.framework.utils;

import com.jcabi.manifests.Manifests;

public class Version {
    public static String getVersion() {
        if (Manifests.exists("Bundle-Build")) {
            return Manifests.read("Bundle-Build");
        } else {
            return "local-build";
        }
    }
}
