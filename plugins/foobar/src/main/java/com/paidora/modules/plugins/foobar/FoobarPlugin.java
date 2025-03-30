package com.paidora.modules.plugins.foobar;

import com.paidora.framework.modules.Module;
import com.paidora.modules.lib.IPlugin;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Map;

/**
 * Плагин, который на запрос Foo вернет bar, а на остальные запросы вернет null.
 * Можно настраивать чувствительность к регистру
 */
@Configurable
@Module("foobar")
public class FoobarPlugin implements IPlugin {
    private boolean ignoreCase = true;

    @Override
    public void start(Map<String, String> params) {
        ignoreCase = Boolean.parseBoolean(params.getOrDefault("ignoreCase", "true"));
    }

    @Override
    public String doSomething(String input) {
        if (ignoreCase ? input.equalsIgnoreCase("foo") : input.equals("Foo")) {
            return "Bar";
        }
        return null;
    }
}
