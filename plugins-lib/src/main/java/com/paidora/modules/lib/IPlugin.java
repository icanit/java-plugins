package com.paidora.modules.lib;

import java.util.Map;

public interface IPlugin {

    default void start(Map<String, String> params) {
    }

    String doSomething(String input);

    default void stopAndDestroy() {
    }
}
