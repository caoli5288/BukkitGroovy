package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.util.Utils;
import groovy.lang.Closure;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
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

public class Listeners {

    private static final Pattern CLASS_NAMES = Pattern.compile("(.*)\\.class");

    private final Map<String, HandlerList> knownClasses = new HashMap<>();

    public void listen(GroovyHandler handler, String name, EventPriority priority, Closure<?> closure) {
        HandlerList handlers = Objects.requireNonNull(getHandlers(name), "event " + name + " not found");
        closure.setDelegate(handler);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        handlers.register(new RegisteredListener(handler, (__, e) -> closure.call(e), priority, handler, false));
    }

    void loadClasses() {
        loadClasses(null, Bukkit.class, s -> s.startsWith("org/bukkit/event"));
    }

    public HandlerList getHandlers(String name) {
        if (!knownClasses.containsKey(name)) {
            int split = name.indexOf('_');
            if (split != -1) {
                Plugin p = Bukkit.getPluginManager().getPlugin(name.substring(0, split));
                if (p != null) {
                    loadClasses(p.getName(), p.getClass(), s -> true);
                }
            }
        }
        return knownClasses.get(name);
    }

    @SneakyThrows
    private void loadClasses(String ns, Class<?> enter, Predicate<String> filter) {
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
            if (ns == null) {
                knownClasses.put(cls.getSimpleName(), handlers);
            } else {
                knownClasses.put(ns + "_" + cls.getSimpleName(), handlers);
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

    public Map<String, HandlerList> getKnownClasses() {
        return knownClasses;
    }
}
