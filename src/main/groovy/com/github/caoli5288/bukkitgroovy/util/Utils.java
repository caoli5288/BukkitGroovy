package com.github.caoli5288.bukkitgroovy.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class Utils {

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

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
