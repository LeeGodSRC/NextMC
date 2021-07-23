package org.fairy.next.entity

import org.fairy.next.entity.Entity
import org.fairy.next.entity.EntityList
import java.util.concurrent.ConcurrentHashMap

class EntityStorage {

    private val entityMap = ConcurrentHashMap<Long, EntityList>(8192)
    private val globalEntities = ConcurrentHashMap<Int, Entity>(8192)

    /**
     * make sure the entity is ready
     */
    fun add(entity: Entity) {
        this.globalEntities[entity.id] = entity

        val entities = this.entityMap.computeIfAbsent(entity.chunkKey) { EntityList() }
        entities.add(entity)
    }

    fun remove(entity: Entity) {
        this.globalEntities.remove(entity.id)

        val chunkKey = entity.chunkKey
        this.entityMap[chunkKey]?. let {
            it.remove(entity)
            if (it.isEmpty()) {
                this.entityMap.remove(chunkKey)
            }
        }
    }

}