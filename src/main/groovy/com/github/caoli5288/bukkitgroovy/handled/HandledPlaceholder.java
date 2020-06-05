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
    private boolean registered;

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        return String.valueOf(closure.call(p, params));
    }

    @Override
    public void cancel() {
        if (registered) {
            registered = false;
            PlaceholderAPI.unregisterPlaceholderHook(id);
        }
    }

    public boolean register() {
        if (isCancelled()) {
            boolean result = PlaceholderAPI.registerPlaceholderHook(id, this);
            if (result) {
                return (registered = true);
            }
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return !registered;
    }
}
