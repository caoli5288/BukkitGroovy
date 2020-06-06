# BukkitGroovy

[![](https://jitpack.io/v/caoli5288/bukkitgroovy.svg)](https://jitpack.io/#caoli5288/bukkitgroovy)

## Groovy dsl style plugins

Create a folder called `plugins/BukkitGroovy/<name>`. The folder contains file named `plugin.groovy`. Here's an example.

```groovy
// plugin.groovy

commands {
    hello {
        it.sendMessage "hello!"
    }

    me { sender, args ->
        runCommand "${sender.name} say ${args.join(" ")}"
    }
}

listeners {
    // builtin event
    PlayerJoinEvent {
        def player = it.player
        def count = 0
        task 20, 20, {
            player.sendMessage "greeting!"
            count++
            if (count >= 5) {
                it.cancel()
            }
        }
    }
    // third-party event
    BedwarsRel_BedwarsGameOverEvent {
        server.shutdown()
    }
}
// events with priority
listeners "HIGHEST", {
    EntityDamageByEntityEvent {
        it.cancelled = true
    }
    EntityDamageByBlockEvent {
        it.cancelled = true
    }
}

placeholders {
    hello { p, params ->
        "$params by ${p.name}"
    }
}

enable {
    logger.info "hello, world"
    config.set("") // reads config.yml in plugins/BukkitGroovy/<name>/
    saveConfig()
    
    a = getResource("a.txt")
    if (a != null) {
        println a.text
    }   
    runCommand "say hahah"
}

disable {
    server.shutdown()
}

version = "x.y.z"
```

## Groovy scripts

Create a custom `GroovyHandler`. Here's an example.

```groovy
// Foo.groovy

import com.github.caoli5288.bukkitgroovy.GroovyHandler

class Foo extends GroovyHandler {

    void onEnable() {
        addCommand("hello", { sender, args -> it.sendMessage "hello!" })
        // just like common java plugins but use groovy-lang
    }

    
}
```

Create `plugin.groovy` includes line.

```groovy
// plugin.groovy
handler = new Foo()
```

## Groovy oneshot scripts

Create file like `plugin/BukkitGroovy/<name>.groovy` and execute `/groovy run <name>`. Here is an example.

```groovy
// sample.groovy

sender.sendMessage args.toString()
```

## Snapshot releases

[![](https://jitpack.io/v/caoli5288/bukkitgroovy.svg)](https://jitpack.io/com/github/caoli5288/bukkitgroovy/master-SNAPSHOT/bukkitgroovy-master-SNAPSHOT.jar)
