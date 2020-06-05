package com.github.caoli5288.bukkitgroovy.util;

import groovy.lang.Closure;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public interface Traits extends Listener {

    default void runCommand(String command) {
        Server server = ((Plugin) this).getServer();
        server.dispatchCommand(server.getConsoleSender(), command);
    }

    default BukkitTask task(Closure<?> closure) {
        return new HandledTask(closure).runTask((Plugin) this);
    }

    default BukkitTask task(int delay, Closure<?> closure) {
        return new HandledTask(closure).runTaskLater((Plugin) this, delay);
    }

    default BukkitTask task(int delay, int repeat, Closure<?> closure) {
        return new HandledTask(closure).runTaskTimer((Plugin) this, delay, repeat);
    }

    default void apply(Closure<?> closure) {
        closure.setDelegate(this);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }
}
