package com.github.caoli5288.bukkitgroovy.dsl

import com.github.caoli5288.bukkitgroovy.util.Utils
import org.bukkit.command.CommandSender
import org.bukkit.event.EventPriority

import java.util.function.BiConsumer

class GroovyObj {

    Closure enable
    Closure disable
    Commands commands = new Commands()
    Listeners listeners = new Listeners()

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

    class Listeners {

        def listeners = [:]

        def invokeMethod(String name, def params) {
            params = params as List
            if (params.size() == 1) {
                listeners[name] = new ListenerObj(closure: params[0], order: EventPriority.NORMAL)
            } else {
                listeners[name] = new ListenerObj(closure: params[1], order: EventPriority.valueOf(params[0] as String))
            }
        }

        def each(BiConsumer<String, ListenerObj> consumer) {
            listeners.each { consumer(it.key, it.value) }
        }
    }

    class ListenerObj {

        EventPriority order
        Closure closure;
    }

    class Commands {

        def commands = [:]

        int size() { commands.size() }

        def invokeMethod(String name, def params) {
            params = params as List
            commands[name] = params[0]
        }

        def execute(def delegate, String name, CommandSender sender, String[] params) {
            if (commands.containsKey(name)) {
                Closure closure = commands[name] as Closure
                closure.delegate = delegate
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure(sender, params)
            }
        }

        def inject(def description) {
            def keys = [:]
            commands.each {
                keys[it.key] = [:]
            }
            Utils.PLUGIN_DESCRIPTION_FILE_commands.set(description, keys)
        }
    }
}
