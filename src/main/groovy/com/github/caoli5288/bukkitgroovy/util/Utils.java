package com.github.caoli5288.bukkitgroovy.util;

import org.bukkit.plugin.PluginDescriptionFile;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Utils {

    public static final Field PLUGIN_DESCRIPTION_FILE_commands = getAccessibleField(PluginDescriptionFile.class, "commands");

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
}
