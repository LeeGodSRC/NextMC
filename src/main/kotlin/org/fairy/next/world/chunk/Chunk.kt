package org.fairy.next.world.chunk

import org.fairy.next.entity.EntityList
import org.fairy.next.util.ChunkCoords

class Chunk(val coord: ChunkCoords) {

    val entities = EntityList()
    val hash: Long by lazy { coord.hash() }

}