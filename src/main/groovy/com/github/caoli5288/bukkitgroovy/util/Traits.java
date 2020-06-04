package com.github.caoli5288.bukkitgroovy.util;

import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public interface Traits {

    default void runCommand(String command) {
        Server server = Bukkit.getServer();
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

    @RequiredArgsConstructor
    class HandledTask extends BukkitRunnable {

        private final Closure<?> closure;

        @Override
        public void run() {
            closure.call(this);
        }
    }
}
