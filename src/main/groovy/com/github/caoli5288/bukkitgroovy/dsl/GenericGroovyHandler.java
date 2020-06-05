package com.github.caoli5288.bukkitgroovy.dsl;

import com.github.caoli5288.bukkitgroovy.GroovyHandler;
import com.github.caoli5288.bukkitgroovy.handled.HandledPlaceholder;
import com.github.caoli5288.bukkitgroovy.handled.ICancellable;
import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class GenericGroovyHandler extends GroovyHandler {

    private final List<ICancellable> holders = new ArrayList<>();
    private final GroovyObj groovyObj;

    @Override
    public void onEnable() {
        Closure<?> enable = groovyObj.getEnable();
        if (enable != null) {
            apply(enable);
        }
        groovyObj.getCommands().each((name, params) -> addCommand(name, (Closure<?>) params.get(0)));
        groovyObj.getListeners().each((eventName, params) -> getPluginLoader().getListeners().listen(this, eventName, ListenerObj.valueOf(params)));
        groovyObj.getPlaceholders().each((name, params) -> holder(name, (Closure<?>) params.get(0)));
    }

    @Override
    public void onDisable() {
        Closure<?> closure = groovyObj.getDisable();
        if (closure != null) {
            apply(closure);
        }
        for (ICancellable cancellable : holders) {
            cancellable.cancel();
        }
        holders.clear();
    }

    private void holder(String name, Closure<?> closure) {
        closure.setDelegate(this);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        HandledPlaceholder placeholder = new HandledPlaceholder(name, closure);
        boolean result = placeholder.register();
        if (result) {
            holders.add(placeholder);
        } else {
            getLogger().warning("Cannot register placeholder " + name);
        }
    }
}
