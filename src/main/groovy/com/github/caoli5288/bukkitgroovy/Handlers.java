package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.dsl.GenericGroovyHandler;
import com.github.caoli5288.bukkitgroovy.dsl.GroovyObj;
import com.github.caoli5288.bukkitgroovy.util.Utils;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.util.DelegatingScript;
import groovy.util.GroovyScriptEngine;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class Handlers {

    private static final Field SIMPLE_PLUGIN_MANAGER_commandMap = Utils.getAccessibleField(SimplePluginManager.class, "commandMap");
    private static final Field SIMPLE_COMMAND_MAP_knownCommands = Utils.getAccessibleField(SimpleCommandMap.class, "knownCommands");

    private final Map<String, GroovyHandler> handlers = new HashMap<>();
    private final GroovyHandledLoader loader;

    private GroovyScriptEngine _shell;

    Handlers(GroovyHandledLoader loader) {
        this.loader = loader;
    }

    public Map<String, GroovyHandler> getHandlers() {
        return handlers;
    }

    public GroovyScriptEngine getGenericGroovy(BukkitGroovy groovy) {
        if (_shell == null) {
            _shell = createGroovy(groovy.getDataFolder().toString());
        }
        return _shell;
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
        if (!handler.getCommands().isEmpty()) {
            try {
                SimpleCommandMap commandMap = (SimpleCommandMap) SIMPLE_PLUGIN_MANAGER_commandMap.get(pm);
                Map<String, Command> knownCommands = (Map<String, Command>) SIMPLE_COMMAND_MAP_knownCommands.get(commandMap);
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
            GroovyScriptEngine shell = getGenericGroovy(groovy);
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
        if (handlers.containsKey(name)) {
            groovy.getLogger().warning(name + " already exists");
        } else {
            File container = new File(groovy.getDataFolder(), name);
            if (container.isDirectory()) {
                load0(groovy, container);
            }
        }
    }

    private void load0(BukkitGroovy groovy, File container) {
        File enter = new File(container, "plugin.groovy");
        if (enter.isFile()) {
            try {
                GroovyScriptEngine shell = createGroovy(container.toString());
                DelegatingScript script = (DelegatingScript) shell.createScript("plugin.groovy", new Binding());
                GroovyObj obj = new GroovyObj();
                script.setDelegate(obj);
                script.run();
                // load handler
                GroovyHandler handler = obj.getHandler();
                if (handler == null) {
                    handler = new GenericGroovyHandler(obj);
                }
                handler.init(loader, container, new PluginDescriptionFile(container.getName(), obj.getVersion(), "plugin.groovy"));
                handlers.put(handler.getName(), handler);
                // and then enable it
                groovy.getServer().getPluginManager().enablePlugin(handler);
            } catch (Exception e) {
                groovy.getLogger().log(Level.SEVERE, "Exception occurred while loading " + enter, e);
            }
        }
    }

    @SneakyThrows
    private static GroovyScriptEngine createGroovy(String container) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(DelegatingScript.class.getName());
        return new GroovyScriptEngine(container, new GroovyClassLoader(BukkitGroovy.class.getClassLoader(), config));
    }

    public static void registerCommand(GroovyHandler handler, Command command) {
        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) SIMPLE_PLUGIN_MANAGER_commandMap.get(handler.getServer().getPluginManager());
            commandMap.register(handler.getName(), command);
        } catch (Exception e) {
        }
    }
}
