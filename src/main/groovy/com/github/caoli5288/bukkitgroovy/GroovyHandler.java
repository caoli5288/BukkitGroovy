package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.dsl.GroovyObj;
import com.github.caoli5288.bukkitgroovy.util.IDsl;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroovyHandler extends PluginBase implements Listener, IDsl {

    private final BukkitGroovy parent;
    private final File container;
    private final GroovyObj groovyObj;
    private final PluginDescriptionFile description;
    private final PluginLogger logger;

    private FileConfiguration config;
    private boolean enabled;
    private boolean sound = true;

    public GroovyHandler(BukkitGroovy parent, File container, GroovyObj groovyObj) {
        this.parent = parent;
        this.container = container;
        this.groovyObj = groovyObj;
        description = new PluginDescriptionFile(container.getName(), "1.0", "unknown");
        groovyObj.getCommands().inject(description);
        logger = new PluginLogger(this);
    }

    public GroovyObj getGroovyObj() {
        return groovyObj;
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

    public PluginLoader getPluginLoader() {
        return parent;
    }

    public Server getServer() {
        return parent.getServer();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void onDisable() {
        apply(groovyObj.getDisable());
    }

    public void onLoad() {

    }

    public void onEnable() {
        // delegated functions first
        apply(groovyObj.getEnable());
    }

    public boolean isNaggable() {
        return sound;
    }

    public void setNaggable(boolean b) {
        sound = b;
    }

    public ChunkGenerator getDefaultWorldGenerator(String world, String id) {
        return parent.getDefaultWorldGenerator(world, id);
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] params) {
        try {
            groovyObj.getCommands().execute(label, sender, Arrays.asList(params));
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("Exception occurred while execute command /%s %s", label, String.join(" ", params)));
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