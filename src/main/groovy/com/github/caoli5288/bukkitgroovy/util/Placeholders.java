package com.github.caoli5288.bukkitgroovy.util;

import com.github.caoli5288.bukkitgroovy.GroovyHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.entity.Player;

public class Placeholders {

    private static final Multimap<String, String> HANDLERS = ArrayListMultimap.create();

    public static void register(GroovyHandler handler, String name, Closure<?> closure) {
        boolean result = PlaceholderAPI.registerPlaceholderHook(name, new ClosureWrapper(closure));
        Preconditions.checkState(result, "Cannot register placeholder " + name);
        HANDLERS.put(handler.getName(), name);
    }

    public static void unregister(GroovyHandler handler) {
        if (HANDLERS.containsKey(handler.getName())) {
            for (String place : HANDLERS.removeAll(handler.getName())) {
                PlaceholderAPI.unregisterPlaceholderHook(place);
            }
        }
    }

    @RequiredArgsConstructor
    private static class ClosureWrapper extends PlaceholderHook {

        private final Closure<?> closure;

        @Override
        public String onPlaceholderRequest(Player player, String param) {
            int parameters = closure.getMaximumNumberOfParameters();
            if (parameters >= 2) {
                return String.valueOf(closure.call(player, param));
            }
            return String.valueOf(closure.call(player));
        }
    }
}
