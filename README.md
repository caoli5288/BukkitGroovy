# BukkitGroovy

[![](https://jitpack.io/v/caoli5288/bukkitgroovy.svg)](https://jitpack.io/#caoli5288/bukkitgroovy)

## Groovy dsl style plugins

Create a folder `plugins/BukkitGroovy/<name>` and put `plugin.groovy` into it. Here is an `plugin.groovy` example.

```groovy
commands {
    hello { sender, args ->
        sender.sendMessage "hello!"
    }

    greeting { sender, args ->
        sender.sendMessage "greeting!"
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

    // event with priority
    PlayerDeathEvent "HIGH", {
        it.player.sendMessage "you death!"
    }
    
    // third-party event
    BedwarsRel_BedwarsGameOverEvent {
        server.shutdown()
    }
}

placeholders {
    hello { p, params ->
        "$params ${p.name}"
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
```

## Groovy scripts

Create single file like `plugin/BukkitGroovy/<name>.groovy` and execute `/groovy run <name>`. Here is an example.

```groovy
// sample.groovy
sender.sendMessage args.toString()
```

## Snapshot releases

[![](https://jitpack.io/v/caoli5288/bukkitgroovy.svg)](https://jitpack.io/com/github/caoli5288/bukkitgroovy/master-SNAPSHOT/bukkitgroovy-master-SNAPSHOT.jar)
