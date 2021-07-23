package org.fairy.next.world

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newSingleThreadContext
import org.fairy.next.entity.Entity
import org.fairy.next.entity.EntityStorage
import org.fairy.next.world.chunk.ChunkStorage
import org.fairy.next.thread.Threading

class World(val name: String) {

    val entityStorage = EntityStorage()
    val chunkStorage = ChunkStorage()
    var difficulty = Difficulty.EASY

    private val coroutineDispatcher = newSingleThreadContext(Threading.PREFIX + "World $name")
    val coroutineScope : CoroutineScope by lazy {
        CoroutineScope(coroutineDispatcher)
    }

    fun tickMain() {
        this.chunkStorage.tickMain()
    }

    fun addEntity(entity: Entity) {
        this.entityStorage.add(entity)
    }

    fun removeEntity(entity: Entity) {
        this.entityStorage.remove(entity)
    }

    suspend fun tick() {
        chunkStorage.tick()
    }

}