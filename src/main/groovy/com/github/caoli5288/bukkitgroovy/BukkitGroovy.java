package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.util.MavenLibs;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class BukkitGroovy extends JavaPlugin implements PluginLoader {

    private Listeners listeners;
    private Handlers handlers;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        try {
            Class.forName("groovy.lang.GroovyShell");
        } catch (ClassNotFoundException e) {
            MavenLibs.load("org.codehaus.groovy", "groovy", "3.0.4");
        }
    }

    @Override
    public void onEnable() {
        listeners = new Listeners();
        listeners.loadClasses();
        handlers = new Handlers();
        getLogger().info(String.format("find %s builtin event classes", listeners.getKnownClasses().size()));
        getServer().getScheduler().runTask(this, () -> handlers.loads(this));
    }

    public Listeners getListeners() {
        return listeners;
    }

    @Override
    public Plugin loadPlugin(File file) throws UnknownDependencyException {
        return null;// noop
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        return null;// noop
    }

    @Override
    public Pattern[] getPluginFileFilters() {
        return null;// noop
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        return getPluginLoader().createRegisteredListeners(listener, plugin);
    }

    public void enablePlugin(Plugin param) {
        GroovyHandler plugin = (GroovyHandler) param;
        if (!plugin.isEnabled()) {
            plugin.getLogger().info("Enabling " + plugin.getDescription().getFullName());
            try {
                plugin.setEnabled(true);
            } catch (Exception e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "Exception occurred while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", e);
            }
            plugin.getServer().getPluginManager().callEvent(new PluginEnableEvent(plugin));
        }
    }

    public void disablePlugin(Plugin param) {
        GroovyHandler plugin = (GroovyHandler) param;
        if (plugin.isEnabled()) {
            plugin.getLogger().info("Disabling " + plugin.getDescription().getFullName());
            plugin.getServer().getPluginManager().callEvent(new PluginDisableEvent(plugin));
            try {
                plugin.setEnabled(false);
            } catch (Exception e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "Error occurred while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", e);
            }
        }
    }
}
