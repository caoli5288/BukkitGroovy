package com.github.caoli5288.bukkitgroovy.handled;

import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class HandledPlaceholder extends PlaceholderHook implements ICancellable {

    private final String id;
    private final Closure<?> closure;
    private boolean cancelled = true;

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        int parameters = closure.getMaximumNumberOfParameters();
        if (parameters >= 2) {
            return String.valueOf(closure.call(p, params));
        }
        return String.valueOf(closure.call(p));
    }

    @Override
    public void cancel() {
        if (!cancelled) {
            cancelled = true;
            PlaceholderAPI.unregisterPlaceholderHook(id);
        }
    }

    public boolean register() {
        if (cancelled && PlaceholderAPI.registerPlaceholderHook(id, this)) {
            cancelled = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
