package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.util.MavenLibs;
import com.github.caoli5288.bukkitgroovy.util.Traits;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class BukkitGroovy extends JavaPlugin implements PluginLoader, Traits {

    private Map<String, BiConsumer<CommandSender, String[]>> commands;
    private Listeners listeners;
    private Handlers handlers;

    @Override
    public void onLoad() {
        try {
            Class.forName("groovy.lang.GroovyShell");
        } catch (ClassNotFoundException e) {
            MavenLibs.load("org.codehaus.groovy", "groovy", "3.0.4");
        }
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        listeners = new Listeners();
        handlers = new Handlers();
        // commands
        commands = new HashMap<>();
        commands.put("list", this::list);
        commands.put("reloads", this::reloads);
        commands.put("loads", this::loads);
        commands.put("unloads", this::unloads);
        commands.put("reload", this::reload);
        commands.put("load", this::load);
        commands.put("unload", this::unload);
        commands.put("run", this::run);
        // bootstraps
        listeners.config(this);
        getLogger().info(String.format("find %s builtin event classes", listeners.getKnownClasses().size() / 2));
        getServer().getScheduler().runTask(this, () -> handlers.loads(this));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
        if (params.length == 0) {
            sender.sendMessage("/groovy list");
            sender.sendMessage("/groovy reloads");
            sender.sendMessage("/groovy loads");
            sender.sendMessage("/groovy unloads");
            sender.sendMessage("/groovy reload <name>");
            sender.sendMessage("/groovy load <name>");
            sender.sendMessage("/groovy unload <name>");
            sender.sendMessage("/groovy run <name> [param...]");
        } else {
            String param = params[0];
            params = Arrays.copyOfRange(params, 1, params.length);
            commands.get(param).accept(sender, params);
            return true;
        }
        return false;
    }

    private void list(CommandSender sender, String[] params) {
        String msg = handlers.getHandlers().keySet().toString();
        sender.sendMessage(msg);
    }

    private void run(CommandSender sender, String[] params) {
        handlers.run(this, sender, params[0], params);
    }

    private void load(CommandSender sender, String[] params) {
        handlers.load(this, params[0]);
    }

    private void unload(CommandSender sender, String[] params) {
        handlers.unload(this, params[0]);
    }

    private void reload(CommandSender sender, String[] params) {
        handlers.reload(this, params[0]);
    }

    private void unloads(CommandSender sender, String[] params) {
        handlers.unloads(this);
    }

    private void loads(CommandSender sender, String[] params) {
        handlers.loads(this);
    }

    private void reloads(CommandSender sender, String[] params) {
        handlers.reloads(this);
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

    public Listeners getListeners() {
        return listeners;
    }
}
