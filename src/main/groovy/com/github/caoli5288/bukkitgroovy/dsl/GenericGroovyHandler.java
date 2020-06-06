package com.github.caoli5288.bukkitgroovy.dsl;

import com.github.caoli5288.bukkitgroovy.BukkitGroovy;
import com.github.caoli5288.bukkitgroovy.GroovyHandler;
import com.github.caoli5288.bukkitgroovy.util.Placeholders;
import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericGroovyHandler extends GroovyHandler {

    private final GroovyObj groovyObj;

    @Override
    public void onEnable() {
        Closure<?> enable = groovyObj.getEnable();
        if (enable != null) {
            apply(enable);
        }
        groovyObj.getCommands().visit((name, params) -> {
            Closure<?> closure = (Closure<?>) params.get(0);
            closure.setDelegate(this);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            addCommand(name, closure);
        });
        for (ListenerObj obj : groovyObj.getListeners()) {
            BukkitGroovy.getListeners().listen(this, obj.getName(), obj.getPriority(), obj.getClosure());
        }
        groovyObj.getPlaceholders().visit((name, params) -> {
            Closure<?> closure = (Closure<?>) params.get(0);
            closure.setDelegate(this);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            Placeholders.register(this, name, closure);
        });
    }

    @Override
    public void onDisable() {
        Closure<?> closure = groovyObj.getDisable();
        if (closure != null) {
            apply(closure);
        }
    }
}
