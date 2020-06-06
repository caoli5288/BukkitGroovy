package com.github.caoli5288.bukkitgroovy.util;

import com.github.caoli5288.bukkitgroovy.GroovyHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import groovy.lang.Closure;

public class Placeholders {

    private static final Multimap<String, PlaceholderClosureWrapper> HANDLERS = ArrayListMultimap.create();

    public static void register(GroovyHandler handler, String name, Closure<?> closure) {
        PlaceholderClosureWrapper wrapper = new PlaceholderClosureWrapper(name, closure);
        boolean result = wrapper.register();
        Preconditions.checkState(result, "Cannot register placeholder " + name);
        HANDLERS.put(handler.getName(), wrapper);
    }

    public static void unregister(GroovyHandler handler) {
        if (HANDLERS.containsKey(handler.getName())) {
            for (PlaceholderClosureWrapper wrapper : HANDLERS.removeAll(handler.getName())) {
                wrapper.unregister();
            }
        }
    }
}
