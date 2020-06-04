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
    playerjoinevent {
        it.player.sendMessage "greeting!"
    }
    
    BedwarsRel_bedwarsgameoverevent {
        server.shutdown()
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