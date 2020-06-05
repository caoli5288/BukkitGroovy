package com.github.caoli5288.bukkitgroovy.dsl;

import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventPriority;

import java.util.List;

@RequiredArgsConstructor
public class ListenerObj {

    private final EventPriority order;
    private final Closure<?> closure;

    public EventPriority getOrder() {
        return order;
    }

    public Closure<?> getClosure() {
        return closure;
    }

    public static ListenerObj valueOf(List<?> in) {
        if (in.size() == 2) {
            return new ListenerObj(EventPriority.valueOf(in.get(0).toString()), (Closure<?>) in.get(1));
        }
        return new ListenerObj(EventPriority.NORMAL, (Closure<?>) in.get(0));
    }
}
