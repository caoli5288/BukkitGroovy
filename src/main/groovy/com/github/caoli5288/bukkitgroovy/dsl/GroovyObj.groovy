package com.github.caoli5288.bukkitgroovy.dsl

import com.github.caoli5288.bukkitgroovy.GroovyHandler

import java.util.function.BiConsumer

class GroovyObj {

    String version = "1.0"
    Closure enable
    Closure disable
    Contexts commands = new Contexts()
    Contexts listeners = new Contexts()
    Contexts placeholders = new Contexts()
    GroovyHandler handler

    def enable(Closure closure) {
        enable = closure
    }

    def disable(Closure closure) {
        disable = closure
    }

    def commands(Closure closure) {
        commands.with closure
    }

    def listeners(Closure closure) {
        listeners.with closure
    }

    def placeholders(Closure closure) {
        placeholders.with closure
    }

    class Contexts {

        def contents = [:]

        def invokeMethod(String name, def params) {
            contents[name] = params
        }

        def each(BiConsumer<String, List<?>> consumer) { contents.each { k, v -> consumer(k, v as List) } }

        int size() { contents.size() }

        List get(String name) { contents[name] as List }

        boolean contains(String name) { contents.containsKey(name) }
    }
}
