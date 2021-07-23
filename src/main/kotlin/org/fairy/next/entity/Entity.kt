package org.fairy.next.entity

import org.fairy.next.util.Location
import org.fairy.next.util.toHash
import java.util.concurrent.atomic.AtomicInteger

open class Entity {

    private companion object {

        private val ENTITY_ID_ATOMIC = AtomicInteger()
        val ENTITY_ID: Int
            get() = this.ENTITY_ID_ATOMIC.getAndIncrement()

    }

    val id = ENTITY_ID
    var location = Location("none", 0, 0, 0)

    val chunkKey: Long
        get() = toHash(location.chunkX, location.chunkZ)

    fun spawn() {

    }

}