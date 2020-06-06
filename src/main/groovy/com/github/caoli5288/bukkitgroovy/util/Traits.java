package com.github.caoli5288.bukkitgroovy.util;

import groovy.lang.Closure;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public interface Traits {

    default void runCommand(String command) {
        Server server = Bukkit.getServer();
        server.dispatchCommand(server.getConsoleSender(), command);
    }

    default void apply(Closure<?> closure) {
        closure.setDelegate(this);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    default String format(Player p, String msg) {
        return PlaceholderAPI.setPlaceholders(p, msg);
    }
}
