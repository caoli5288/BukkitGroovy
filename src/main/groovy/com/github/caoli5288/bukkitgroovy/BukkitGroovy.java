package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.util.MavenLibs;
import com.github.caoli5288.bukkitgroovy.util.Traits;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class BukkitGroovy extends JavaPlugin implements Traits {

    private static BukkitGroovy _this;

    private Map<String, BiConsumer<CommandSender, String[]>> commands;
    private Listeners listeners;
    private Handlers handlers;

    @Override
    public void onLoad() {
        _this = this;
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
        handlers = new Handlers(new GroovyHandledLoader(getPluginLoader()));
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

    public static Listeners getListeners() {
        return _this.listeners;
    }
}
