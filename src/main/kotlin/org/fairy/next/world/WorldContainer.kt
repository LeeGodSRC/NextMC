package org.fairy.next.world

import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class WorldContainer {

    private val worlds : MutableMap<String, World> = ConcurrentHashMap()

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

}