package com.github.caoli5288.bukkitgroovy.util;

import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PlaceholderClosureWrapper extends PlaceholderHook {

    private final String name;
    private final Closure<?> closure;
    private boolean registered;

    @Override
    public String onPlaceholderRequest(Player player, String param) {
        int parameters = closure.getMaximumNumberOfParameters();
        if (parameters == 2) {
            return String.valueOf(closure.call(player, param));
        }
        return String.valueOf(closure.call(player));
    }

    public boolean register() {
        return registered = PlaceholderAPI.registerPlaceholderHook(name, this);
    }

    void unregister() {
        if (registered) {
            registered = false;
            PlaceholderAPI.unregisterPlaceholderHook(name);
        }
    }
}
