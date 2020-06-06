package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.util.Placeholders;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class GroovyHandledLoader implements PluginLoader {

    private final PluginLoader handle;

    @Override
    public Plugin loadPlugin(File file) throws UnknownDependencyException {
        throw new UnsupportedOperationException("loadPlugin");
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) {
        throw new UnsupportedOperationException("getPluginDescription");
    }

    @Override
    public Pattern[] getPluginFileFilters() {
        throw new UnsupportedOperationException("getPluginFileFilters");
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        return handle.createRegisteredListeners(listener, plugin);
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        GroovyHandler handler = (GroovyHandler) plugin;
        if (!handler.isEnabled()) {
            handler.getLogger().info("Enabling " + handler.getDescription().getFullName());
            try {
                handler.setEnabled(true);
            } catch (Exception e) {
                handler.getServer().getLogger().log(Level.SEVERE, "Exception occurred while enabling " + handler.getDescription().getFullName() + " (Is it up to date?)", e);
            }
            handler.getServer().getPluginManager().callEvent(new PluginEnableEvent(handler));
        }
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        GroovyHandler handler = (GroovyHandler) plugin;
        if (handler.isEnabled()) {
            handler.getLogger().info("Disabling " + handler.getDescription().getFullName());
            handler.getServer().getPluginManager().callEvent(new PluginDisableEvent(handler));
            try {
                handler.setEnabled(false);
            } catch (Exception e) {
                handler.getServer().getLogger().log(Level.SEVERE, "Error occurred while disabling " + handler.getDescription().getFullName() + " (Is it up to date?)", e);
            }
            handler.getCommands().clear();
            Placeholders.unregister(handler);
        }
    }
}
