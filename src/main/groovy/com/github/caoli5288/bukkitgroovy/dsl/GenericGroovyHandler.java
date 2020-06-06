package com.github.caoli5288.bukkitgroovy.dsl;

import com.github.caoli5288.bukkitgroovy.BukkitGroovy;
import com.github.caoli5288.bukkitgroovy.GroovyHandler;
import com.github.caoli5288.bukkitgroovy.Listeners;
import com.github.caoli5288.bukkitgroovy.util.Placeholders;
import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventPriority;

@RequiredArgsConstructor
public class GenericGroovyHandler extends GroovyHandler {

    private final GroovyObj groovyObj;

    @Override
    public void onEnable() {
        Closure<?> enable = groovyObj.getEnable();
        if (enable != null) {
            apply(enable);
        }
        groovyObj.getCommands().each((name, params) -> {
            Closure<?> closure = (Closure<?>) params.get(0);
            closure.setDelegate(this);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            addCommand(name, closure);
        });
        groovyObj.getListeners().each((name, params) -> {
            Listeners listeners = BukkitGroovy.getListeners();
            if (params.size() == 1) {
                listeners.listen(this, name, EventPriority.NORMAL, (Closure<?>) params.get(0));
            } else {
                listeners.listen(this, name, EventPriority.valueOf(params.get(0).toString()), (Closure<?>) params.get(1));
            }
        });
        groovyObj.getPlaceholders().each((name, params) -> {
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
