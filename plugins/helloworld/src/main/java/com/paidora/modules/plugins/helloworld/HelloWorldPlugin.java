package com.paidora.modules.plugins.helloworld;

import com.paidora.framework.modules.Module;
import com.paidora.modules.lib.IPlugin;
import com.paidora.modules.lib.ISharedRefsSrv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Плагин, который на hello вернет world, а на другие запросы, строку из предоставленного ядром справочника.
 */
@Configurable
@Module("helloworld")
public class HelloWorldPlugin implements IPlugin {
    @Autowired
    protected ISharedRefsSrv sharedRefsSrv;

    @Override
    public String doSomething(String input) {
        if (input.equalsIgnoreCase("Hello")) {
            return "World";
        }
        return sharedRefsSrv.getSomeStringValue();
    }
}
