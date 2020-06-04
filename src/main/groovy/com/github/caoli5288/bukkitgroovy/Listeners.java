package com.github.caoli5288.bukkitgroovy;

import com.github.caoli5288.bukkitgroovy.util.Utils;
import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Listeners {

    private static final Pattern CLASS_NAMES = Pattern.compile("(.*)\\.class");

    private final Map<String, HandlerList> knownClasses = new HashMap<>();
    private final Set<String> loads = new HashSet<>();

    public void listen(GroovyHandler handler, String name, Closure<?> closure) {
        loadClasses(name);
        String lowName = name.toLowerCase();
        if (knownClasses.containsKey(lowName)) {
            listen(handler, knownClasses.get(lowName), closure);
        } else {
            handler.getLogger().warning(String.format("event %s not found", name));
        }
    }

    private void listen(GroovyHandler handler, HandlerList handlers, Closure<?> closure) {
        closure.setDelegate(handler);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        handlers.register(new RegisteredListener(handler, (__, e) -> closure.call(e), EventPriority.NORMAL, handler, false));
    }

    public void loadClasses() {
        if (loads.add("")) {
            loadClasses("minecraft", Bukkit.class, "org/bukkit/event");
        }
    }

    private void loadClasses(String name) {
        int split = name.indexOf('_');
        if (split != -1) {
            String pluginName = name.substring(0, split);
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin != null && loads.add(plugin.getName())) {
                Class<?> cls = plugin.getClass();
                loadClasses(plugin.getName().toLowerCase(), cls, "");
            }
        }
    }

    private void loadClasses(String namespace, Class<?> enter, String path) {
        ClassLoader cl = enter.getClassLoader();
        try {
            JarFile jar = new JarFile(enter.getProtectionDomain().getCodeSource().getLocation().getFile());
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(path) && CLASS_NAMES.matcher(name).matches()) {
                    name = name.replace('/', '.').substring(0, name.length() - 6);
                    try {
                        Class<?> cls = cl.loadClass(name);
                        if (Event.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())) {
                            HandlerList handlers = lookupHandlers(cls);
                            String simpleName = cls.getSimpleName().toLowerCase();
                            knownClasses.put(namespace + "_" + simpleName, handlers);
                            if (!knownClasses.containsKey(simpleName)) {
                                knownClasses.put(simpleName, handlers);
                            }
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().log(Level.SEVERE, "Exception occurred while lookup event class " + name, e);
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Exception occurred while lookup event classes", e);
        }
    }

    private HandlerList lookupHandlers(Class<?> cls) throws ReflectiveOperationException {
        Method method = Utils.getAccessibleMethod(cls, "getHandlerList");
        if (method == null) {
            Preconditions.checkState(cls != Event.class);
            return lookupHandlers(cls.getSuperclass());
        }
        return (HandlerList) method.invoke(cls);
    }

    public Map<String, HandlerList> getKnownClasses() {
        return knownClasses;
    }
}
