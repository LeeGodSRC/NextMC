package org.fairy.next.world

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.fairy.next.world.chunk.ChunkStorage
import org.fairy.next.thread.Threading

class World(val name: String) {

    val chunkStorage = ChunkStorage()

    private val coroutineDispatcher = newSingleThreadContext(Threading.PREFIX + "World $name")
    val coroutineScope : CoroutineScope by lazy {
        CoroutineScope(coroutineDispatcher)
    }

    fun tickMain() {
        this.chunkStorage.tickMain()
    }

    suspend fun tick() {
        chunkStorage.tick()
    }

}