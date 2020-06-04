package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.dsl.GroovyObj;
import com.github.caoli5288.bukkitgroovy.util.Utils;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.util.DelegatingScript;
import groovy.util.GroovyScriptEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class Handlers {

    private static final Field SIMPLE_PLUGIN_MANAGER_commandMap = Utils.getAccessibleField(SimplePluginManager.class, "commandMap");
    private static final Field SimpleCommandMap_knownCommands = Utils.getAccessibleField(SimpleCommandMap.class, "commandMap");

    private final Map<String, GroovyHandler> handlers = new HashMap<>();
    private GroovyScriptEngine shell;

    public Map<String, GroovyHandler> getHandlers() {
        return handlers;
    }

    public GroovyScriptEngine getShell(BukkitGroovy groovy) {
        if (shell == null) {
            CompilerConfiguration config = new CompilerConfiguration();
            config.setScriptBaseClass(DelegatingScript.class.getName());
            try {
                shell = new GroovyScriptEngine(groovy.getDataFolder().toString(), new GroovyClassLoader(BukkitGroovy.class.getClassLoader(), config));
            } catch (IOException e) {
                groovy.getLogger().log(Level.SEVERE, "Error occurred while create groovy shell", e);
            }
        }
        return shell;
    }

    public void loads(BukkitGroovy groovy) {
        String[] names = groovy.getDataFolder().list();
        Objects.requireNonNull(names);
        for (String name : names) {
            load(groovy, name);
        }
    }

    public void reloads(BukkitGroovy groovy) {
        unloads(groovy);
        loads(groovy);
    }

    public void reload(BukkitGroovy groovy, String name) {
        unload(groovy, name);
        load(groovy, name);
    }

    public void unload(BukkitGroovy groovy, String name) {
        if (handlers.containsKey(name)) {
            GroovyHandler handler = handlers.remove(name);
            unload0(groovy, handler);
        }
    }

    public void unloads(BukkitGroovy groovy) {
        for (GroovyHandler handler : handlers.values()) {
            unload0(groovy, handler);
        }
        handlers.clear();
    }

    @SuppressWarnings("unchecked")
    private void unload0(BukkitGroovy groovy, GroovyHandler handler) {
        PluginManager pm = groovy.getServer().getPluginManager();
        pm.disablePlugin(handler);
        if (handler.getGroovyObj().getCommands().size() != 0) {
            try {
                SimpleCommandMap commandMap = (SimpleCommandMap) SIMPLE_PLUGIN_MANAGER_commandMap.get(pm);
                Map<String, Command> knownCommands = (Map<String, Command>) SimpleCommandMap_knownCommands.get(commandMap);
                Iterator<Command> iterator = knownCommands.values().iterator();
                while (iterator.hasNext()) {
                    Command command = iterator.next();
                    if (command instanceof PluginCommand) {
                        PluginCommand pCommand = (PluginCommand) command;
                        if (pCommand.getPlugin() == handler) {
                            iterator.remove();
                        }
                    }
                }
            } catch (Exception e) {
                groovy.getLogger().log(Level.SEVERE, "Exception occurred while disable " + handler.getName(), e);
            }
        }
    }

    public void run(BukkitGroovy groovy, CommandSender sender, String name, String[] params) {
        File f = new File(groovy.getDataFolder(), name + ".groovy");
        if (f.isFile()) {
            GroovyScriptEngine shell = getShell(groovy);
            try {
                Binding binding = new Binding(params);
                binding.setVariable("sender", sender);
                DelegatingScript script = (DelegatingScript) shell.createScript(f.getName(), binding);
                script.setDelegate(groovy);
                script.run();
            } catch (Exception e) {
                groovy.getLogger().log(Level.SEVERE, "Exception occurred while execute " + f, e);
            }
        } else {
            groovy.getLogger().warning(f + " not found");
        }
    }

    public void load(BukkitGroovy groovy, String name) {
        File container = new File(groovy.getDataFolder(), name);
        if (container.isDirectory()) {
            File f = new File(container, "plugin.groovy");
            if (f.isFile()) {
                if (handlers.containsKey(name)) {
                    groovy.getLogger().warning(name + " already exists");
                } else {
                    CompilerConfiguration config = new CompilerConfiguration();
                    config.setScriptBaseClass(DelegatingScript.class.getName());
                    try {
                        GroovyScriptEngine shell = new GroovyScriptEngine(container.toString(), new GroovyClassLoader(BukkitGroovy.class.getClassLoader(), config));
                        DelegatingScript script = (DelegatingScript) shell.createScript("plugin.groovy", new Binding());
                        GroovyObj obj = new GroovyObj();
                        script.setDelegate(obj);
                        script.run();
                        GroovyHandler handler = new GroovyHandler(groovy, container, obj);
                        handlers.put(name, handler);// put first
                        groovy.getServer().getPluginManager().enablePlugin(handler);
                        obj.getListeners().each((listener, closure) -> groovy.getListeners().listen(handler, listener, closure));
                    } catch (Exception e) {
                        groovy.getLogger().log(Level.SEVERE, "Exception occurred while loading " + f, e);
                    }
                }
            }
        }
    }
}
