package org.fairy.next.entity

import org.fairy.next.util.collection.ObjectMapList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write

open class EntityList {

    private val list = ObjectMapList<Entity>()
    private val lock = ReentrantReadWriteLock()

    private var iterationData: Array<Entity?>? = null
    private var iterationLock = ReentrantLock()
    private val iterationCount = AtomicInteger()

    fun get(id: Int): Entity? = this.lock.read { list[id] }

    fun add(entity: Entity): Boolean = this.lock.write {
        list.add(entity)
    }

    fun remove(entity: Entity): Boolean = this.lock.write {
        list.remove(entity)
    }

    fun isEmpty(): Boolean = this.lock.read {
        list.isEmpty
    }

    fun forEach(consumer: (Entity?) -> Unit) = this.iterationLock.withLock {
        this.iterationData ?: run {
            lock.read {
                this.iterationData = this.list.rawData.clone()
            }
        }

        this.iterationCount.incrementAndGet()
        try {
            this.iterationData!!.forEach(consumer)
        } finally {
            if (this.iterationCount.decrementAndGet() == 0) {
                this.iterationData = null
            }
        }
    }

}