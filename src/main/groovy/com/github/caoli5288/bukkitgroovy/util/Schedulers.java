package com.github.caoli5288.bukkitgroovy.util;

import com.github.caoli5288.bukkitgroovy.GroovyHandler;
import groovy.lang.Closure;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Schedulers extends BukkitRunnable {

    private final Closure<?> closure;

    @Override
    public void run() {
        closure.call(this);
    }

    public static BukkitTask run(GroovyHandler handler, Closure<?> closure) {
        Schedulers wrapper = new Schedulers(closure);
        return wrapper.runTask(handler);
    }

    public static BukkitTask runLater(GroovyHandler handler, int delay, Closure<?> closure) {
        Schedulers wrapper = new Schedulers(closure);
        return wrapper.runTaskLater(handler, delay);
    }

    public static BukkitTask runTimer(GroovyHandler handler, int delay, int repeat, Closure<?> closure) {
        Schedulers wrapper = new Schedulers(closure);
        return wrapper.runTaskTimer(handler, delay, repeat);
    }
}
