package com.github.caoli5288.bukkitgroovy.util;

import groovy.lang.Closure;

public interface IDsl {

    default void apply(Closure<?> closure) {
        if (closure != null) {
            closure.setDelegate(this);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            closure.call();
        }
    }
}
