package com.github.caoli5288.bukkitgroovy.dsl

import com.github.caoli5288.bukkitgroovy.GroovyHandler
import groovy.transform.CompileStatic
import org.bukkit.event.EventPriority

import java.util.function.BiConsumer

@CompileStatic
class GroovyObj {

    String version = "1.0"
    Closure enable
    Closure disable
    Contexts commands = new Contexts()
    Contexts placeholders = new Contexts()
    List<ListenerObj> listeners = []
    GroovyHandler main

    def enable(Closure closure) {
        enable = closure
    }

    def disable(Closure closure) {
        disable = closure
    }

    def commands(Closure closure) {
        commands.with closure
    }

    def listeners(String priority, Closure closure) {
        listeners(EventPriority.valueOf(priority), closure)
    }

    def listeners(EventPriority priority = EventPriority.NORMAL, Closure closure) {
        def context = new Contexts()
        context.with closure
        context.visit() { k, v ->
            listeners << new ListenerObj(priority: priority, name: k, closure: v[0] as Closure)
        }
    }

    def placeholders(Closure closure) {
        placeholders.with closure
    }

    class Contexts {

        Map<String, List<?>> contents = [:]

        def invokeMethod(String name, def params) {
            contents[name] = params as List
        }

        def visit(BiConsumer<String, List<?>> consumer) { contents.each { k, v -> consumer(k, v) } }
    }
}
