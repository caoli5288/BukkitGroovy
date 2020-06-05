package com.github.caoli5288.bukkitgroovy.util;

import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class HandledTask extends BukkitRunnable {

    private final Closure<?> closure;

    @Override
    public void run() {
        closure.call(this);
    }
}
