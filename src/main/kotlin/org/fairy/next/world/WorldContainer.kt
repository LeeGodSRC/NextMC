package org.fairy.next.world

import kotlinx.coroutines.launch
import org.fairy.next.thread.newThread
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class WorldContainer {

    private val worlds : MutableMap<String, World> = ConcurrentHashMap()

    init {
        CompletableFuture.allOf(
            this.create("world"),
            this.create("world_nether"),
            this.create("world_the_end")
        ).join()
    }

    fun tick() {
        this.worlds.forEach { it.value.tickMain() }

        this.worlds.forEach {
            val world = it.value

            world.coroutineScope.launch {
                world.tick()
            }
        }
    }

    fun get(name: String): World? = this.worlds[name]

    fun create(name: String): CompletableFuture<World> {
        val future = CompletableFuture<World>()
        newThread("World Creator - $name") {
            val world = World(name)

            this.worlds[name] = world
            future.complete(world)
        }
        return future
    }

}