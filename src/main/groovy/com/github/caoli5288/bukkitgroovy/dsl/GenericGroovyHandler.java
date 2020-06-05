package com.github.caoli5288.bukkitgroovy.dsl;

import com.github.caoli5288.bukkitgroovy.BukkitGroovy;
import com.github.caoli5288.bukkitgroovy.GroovyHandler;
import com.github.caoli5288.bukkitgroovy.handled.HandledPlaceholder;
import com.github.caoli5288.bukkitgroovy.handled.ICancellable;
import com.github.caoli5288.bukkitgroovy.util.Utils;
import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RequiredArgsConstructor
public class GenericGroovyHandler extends GroovyHandler {

    private final List<ICancellable> cancels = new ArrayList<>();
    private final GroovyObj groovyObj;

    @Override
    public void onEnable() {
        Closure<?> closure = groovyObj.getEnable();
        if (closure != null) {
            apply(closure);
        }
        groovyObj.getListeners().each((eventName, obj) -> BukkitGroovy.getListeners().listen(this, eventName, ListenerObj.valueOf(obj)));
        groovyObj.getPlaceholders().each((id, params) -> {
            HandledPlaceholder placeholder = new HandledPlaceholder(id, (Closure<?>) params.get(0));
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
        try {
            List<?> ctx = groovyObj.getCommands().get(label);
            if (!Utils.isNullOrEmpty(ctx)) {
                Closure<?> closure = (Closure<?>) ctx.get(0);
                int parameters = closure.getMaximumNumberOfParameters();
                if (parameters >= 2) {
                    closure.call(sender, params);
                } else {
                    closure.call(sender);
                }
                return true;
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, String.format("Exception occurred while execute command /%s %s", label, String.join(" ", params)), e);
        }
        return false;
    }
}
