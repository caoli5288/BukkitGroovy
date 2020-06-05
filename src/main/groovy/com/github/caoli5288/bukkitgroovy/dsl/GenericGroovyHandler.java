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

    private final List<ICancellable> cancels = new ArrayList<>();
    private final GroovyObj groovyObj;

    @Override
    public void onEnable() {
        Closure<?> enable = groovyObj.getEnable();
        if (enable != null) {
            apply(enable);
        }
        groovyObj.getCommands().each((name, params) -> addCommand(name, (Closure<?>) params.get(0)));
        groovyObj.getListeners().each((eventName, params) -> getPluginLoader().getListeners().listen(this, eventName, ListenerObj.valueOf(params)));
        groovyObj.getPlaceholders().each((id, params) -> {
            Closure<?> closure = (Closure<?>) params.get(0);
            closure.setDelegate(this);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            HandledPlaceholder placeholder = new HandledPlaceholder(id, closure);
            if (placeholder.register()) {
                cancels.add(placeholder);
            } else {
                getLogger().warning("Cannot register placeholder " + id);
            }
        });
    }

    @Override
    public void onDisable() {
        Closure<?> closure = groovyObj.getDisable();
        if (closure != null) {
            apply(closure);
        }
        for (ICancellable cancellable : cancels) {
            cancellable.cancel();
        }
        cancels.clear();
    }
}
