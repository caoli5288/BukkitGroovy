package com.github.caoli5288.bukkitgroovy.util;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class Utils {

    private static final Constructor<PluginCommand> PLUGIN_COMMAND_CONSTRUCTOR = getAccessibleConstructor(PluginCommand.class, String.class, Plugin.class);

    public static PluginCommand newCommand(String name, Plugin plugin) {
        try {
            return PLUGIN_COMMAND_CONSTRUCTOR.newInstance(name, plugin);
        } catch (Exception e) {
        }
        return null;
    }

    public static Field getAccessibleField(Class<?> cls, String name) {
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
        }
        return null;
    }

    public static Method getAccessibleMethod(Class<?> cls, String name, Class<?>... parameters) {
        try {
            Method method = cls.getDeclaredMethod(name, parameters);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
        }
        return null;
    }

    public static <T> Constructor<T> getAccessibleConstructor(Class<T> cls, Class<?>... parameters) {
        try {
            Constructor<T> constructor = cls.getDeclaredConstructor(parameters);
            constructor.setAccessible(true);
            return constructor;
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEnabled(Plugin plugin) {
        return plugin != null && plugin.isEnabled();
    }
}
