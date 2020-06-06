package com.github.caoli5288.bukkitgroovy.dsl;

import groovy.lang.Closure;
import lombok.Data;
import org.bukkit.event.EventPriority;

@Data
public class ListenerObj {

    private EventPriority priority;
    private String name;
    private Closure<?> closure;
}
