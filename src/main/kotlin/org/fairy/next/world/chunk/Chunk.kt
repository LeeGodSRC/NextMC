package org.fairy.next.world.chunk

import org.fairy.next.org.fairy.next.util.ChunkCoords

class Chunk(val coord: ChunkCoords) {

    val hash: Long by lazy { coord.hash() }

}