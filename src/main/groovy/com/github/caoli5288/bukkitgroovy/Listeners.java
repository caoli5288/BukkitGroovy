package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.util.Utils;
import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import lombok.Data;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class Listeners implements Listener {

    private static final Pattern CLASS_NAMES = Pattern.compile("(.*)\\.class");

    private final Map<String, Classes> knownClasses = new HashMap<>();

    public void listen(GroovyHandler handler, String name, EventPriority priority, Closure<?> closure) {
        Classes classes = getClasses(name);
        Objects.requireNonNull(classes, "Cannot found event " + name);
        closure.setDelegate(handler);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        classes.getHandlers().register(new RegisteredListener(handler, (__, e) -> closure.call(e), priority, handler, false));
    }

    private void loadClasses() {
        loadClasses(null, Bukkit.class, s -> s.startsWith("org/bukkit/event"));
    }

    public Classes getClasses(String name) {
        if (!knownClasses.containsKey(name)) {
            int split = name.indexOf('_');
            if (split != -1) {
                String splitName = name.substring(0, split);
                Plugin plugin = Bukkit.getPluginManager().getPlugin(splitName);
                Preconditions.checkState(Utils.isEnabled(plugin), "Cannot found plugin " + splitName);
                loadClasses(plugin.getName(), plugin.getClass(), s -> true);
            }
        }
        return knownClasses.get(name);
    }

    @SneakyThrows
    public void loadClasses(String ns, Class<?> enter, Predicate<String> filter) {
        ClassLoader cl = enter.getClassLoader();
        JarFile jar = new JarFile(enter.getProtectionDomain().getCodeSource().getLocation().getFile());
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (filter.test(name) && CLASS_NAMES.matcher(name).matches()) {
                name = name.replace('/', '.').substring(0, name.length() - 6);
                try {
                    Class<?> cls = cl.loadClass(name);
                    if (Event.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())) {
                        load0(ns, cls);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private void load0(String ns, Class<?> cls) {
        HandlerList handlers = lookupHandlers(cls);
        if (handlers != null) {
            Classes classes = new Classes(cls, handlers);
            if (ns == null) {
                knownClasses.put(cls.getSimpleName(), classes);
                knownClasses.put(cls.getSimpleName().toLowerCase(), classes);
            } else {
                knownClasses.put(ns + "_" + cls.getSimpleName(), classes);
                knownClasses.put(ns + "_" + cls.getSimpleName().toLowerCase(), classes);
            }
        }
    }

    @SneakyThrows
    @Nullable
    private HandlerList lookupHandlers(Class<?> cls) {
        Method method = Utils.getAccessibleMethod(cls, "getHandlerList");
        if (method == null) {
            Class<?> superCls = cls.getSuperclass();
            if (superCls == Event.class) {
                return null;
            }
            return lookupHandlers(superCls);
        }
        return (HandlerList) method.invoke(cls);
    }

    public Map<String, Classes> getKnownClasses() {
        return knownClasses;
    }

    void config(BukkitGroovy groovy) {
        loadClasses();
        groovy.getServer().getPluginManager().registerEvents(this, groovy);
    }

    @EventHandler
    public void on(PluginDisableEvent event) {
        clearClasses(event.getPlugin().getClass().getClassLoader());
    }

    @EventHandler
    public void on(PluginEnableEvent event) {
        Plugin plugin = event.getPlugin();
        loadClasses(plugin.getName(), plugin.getClass(), s -> true);
    }

    private void clearClasses(ClassLoader cl) {
        knownClasses.values().removeIf(classes -> classes.cls.getClassLoader() == cl);
    }

    @Data
    public static class Classes {

        private final Class<?> cls;
        private final HandlerList handlers;
    }
}
