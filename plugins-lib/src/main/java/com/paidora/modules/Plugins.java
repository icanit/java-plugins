package com.paidora.modules;

public interface Plugins {
    static String getPluginsRoot() {
        return Plugins.class.getPackage().getName() + ".plugins";
    }
}
