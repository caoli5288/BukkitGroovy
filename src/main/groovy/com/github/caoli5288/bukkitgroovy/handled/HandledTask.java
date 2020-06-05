package com.github.caoli5288.bukkitgroovy.handled;

import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class HandledTask extends BukkitRunnable implements ICancellable {

    private final Closure<?> closure;

    @Override
    public void run() {
        closure.call(this);
    }
}
