package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.util.Schedulers;
import com.github.caoli5288.bukkitgroovy.util.Traits;
import com.github.caoli5288.bukkitgroovy.util.Utils;
import com.google.common.io.Files;
import groovy.lang.Closure;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroovyHandler extends PluginBase implements Listener, Traits {

    private final Map<String, Closure<?>> commands = new HashMap<>();

    private GroovyHandledLoader loader;
    private File container;
    private PluginDescriptionFile description;
    private PluginLogger logger;
    private FileConfiguration config;

    private boolean enabled;
    private boolean naggable = true;

    final void init(GroovyHandledLoader loader, File container, PluginDescriptionFile description) {
        this.loader = loader;
        this.container = container;
        this.description = description;
        logger = new PluginLogger(this);
    }

    Map<String, Closure<?>> getCommands() {
        return commands;
    }

    public File getDataFolder() {
        return container;
    }

    public PluginDescriptionFile getDescription() {
        return description;
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    @SneakyThrows
    public InputStream getResource(String name) {
        File f = new File(container, name);
        if (f.isFile()) {
            return new FileInputStream(f);
        }
        return null;
    }

    public void saveConfig() {
        if (config != null) {
            try {
                config.save(new File(container, "config.yml"));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception occurred while save config.", e);
            }
        }
    }

    public void saveDefaultConfig() {
        // noop
    }

    public void saveResource(String name, boolean replace) {
        // noop
    }

    @SneakyThrows
    public void reloadConfig() {
        File f = new File(container, "config.yml");
        if (f.isFile()) {
            config = YamlConfiguration.loadConfiguration(Files.newReader(f, StandardCharsets.UTF_8));
        } else {
            config = new YamlConfiguration();
        }
    }

    public PluginLoader getPluginLoader() {
        return loader;
    }

    public Server getServer() {
        return Bukkit.getServer();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void onDisable() {
    }

    public void onLoad() {
    }

    public void onEnable() {
    }

    public boolean isNaggable() {
        return naggable;
    }

    public void setNaggable(boolean b) {
        naggable = b;
    }

    public ChunkGenerator getDefaultWorldGenerator(String world, String id) {
        return null;
    }

    public Logger getLogger() {
        return logger;
    }

    public void addCommand(String name, Closure<?> closure) {
        if (!commands.containsKey(name)) {
            PluginCommand command = Utils.newCommand(name, this);
            Handlers.registerCommand(this, command);
        }
        commands.put(name, closure);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
        Closure<?> closure = commands.get(command.getName());
        if (closure != null) {
            int parameters = closure.getMaximumNumberOfParameters();
            try {
                if (parameters >= 2) {
                    closure.call(sender, params);
                } else {
                    closure.call(sender);
                }
                return true;
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, String.format("Exception occurred while execute command /%s %s", label, String.join(" ", params)), e);
            }
        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] params) {
        return null;
    }

    protected void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    public BukkitTask task(Closure<?> closure) {
        return Schedulers.run(this, closure);
    }

    public BukkitTask task(int delay, Closure<?> closure) {
        return Schedulers.runLater(this, delay, closure);
    }

    public BukkitTask task(int delay, int repeat, Closure<?> closure) {
        return Schedulers.runTimer(this, delay, repeat, closure);
    }
}
