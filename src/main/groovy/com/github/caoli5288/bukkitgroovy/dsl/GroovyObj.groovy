package com.github.caoli5288.bukkitgroovy.dsl

import com.github.caoli5288.bukkitgroovy.util.Utils
import org.bukkit.command.CommandSender

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
            listeners[name] = params.find()
        }

        def each(BiConsumer<String, Closure> consumer) {
            listeners.each { consumer(it.key, it.value) }
        }
    }

    class Commands {

        def commands = [:]

        int size() { commands.size() }

        def invokeMethod(String name, def params) {
            commands[name] = params.find()
        }

        def execute(String name, CommandSender sender, List<String> params) {
            if (commands.containsKey(name)) {
                commands[name](sender, params)
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
