package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.util.Traits;
import com.github.caoli5288.bukkitgroovy.util.Utils;
import com.google.common.io.Files;
import groovy.lang.Closure;
import lombok.SneakyThrows;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GroovyHandler extends PluginBase implements Traits {

    private final Map<String, Closure<?>> commands = new HashMap<>();

    private BukkitGroovy parent;
    private File container;
    private PluginDescriptionFile description;
    private PluginLogger logger;
    private FileConfiguration config;

    private boolean enabled;
    private boolean naggable = true;

    final void init(BukkitGroovy parent, File container, PluginDescriptionFile description) {
        this.parent = parent;
        this.container = container;
        this.description = description;
        logger = new PluginLogger(this);
    }

    int getCommands() {
        return commands.size();
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

    public InputStream getResource(String name) {
        File f = new File(container, name);
        if (f.isFile()) {
            try {
                return new FileInputStream(f);
            } catch (FileNotFoundException e) {
                logger.log(Level.SEVERE, "Exception occurred while get resource " + name, e);
            }
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

    public BukkitGroovy getPluginLoader() {
        return parent;
    }

    public Server getServer() {
        return parent.getServer();
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
        return parent.getDefaultWorldGenerator(world, id);
    }

    public Logger getLogger() {
        return logger;
    }

    public void addCommand(String name, Closure<?> closure) {
        closure.setDelegate(this);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
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
}
