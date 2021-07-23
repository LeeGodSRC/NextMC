package org.fairy.next.world.chunk

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import org.fairy.next.extension.mc
import org.fairy.next.thread.MainThreadQueue
import org.fairy.next.util.lock.WeakSeqLock
import org.fairy.next.util.toHash
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * The Chunk Storage for Worlds
 */
class ChunkStorage : MainThreadQueue() {

    // Loaded Chunks, Only write on main, Search MT Safe
    private val loadedChunksLock = WeakSeqLock()
    private val loadedChunks = Long2ObjectOpenHashMap<Chunk>(8192, 0.5f)
    private val isWriteAllowed = AtomicBoolean(false)

    // Pending Chunks, MT Safe read/write
    private val pendingChunksLock = ReentrantReadWriteLock()
    private val pendingLoadChunks = ConcurrentHashMap<Long, Chunk>()
    private val pendingUnloadChunks = ConcurrentHashMap.newKeySet<Long>()

    private fun addLoadedChunkMain(chunk: Chunk) {
        if (!this.isWriteAllowed.get()) {
            throw IllegalArgumentException("Writing is not allowed at this stage.")
        }

        if (!mc.isMainThread()) {
            throw IllegalStateException("Writing loaded chunks concurrently is not allowed.")
        }

        this.loadedChunksLock.acquireWrite()
        try {
            this.loadedChunks[chunk.hash] = chunk
        } finally {
            this.loadedChunksLock.releaseWrite()
        }
    }

    private fun removeLoadedChunkMain(chunk: Chunk) {
        if (!this.isWriteAllowed.get()) {
            throw IllegalArgumentException("Writing is not allowed at this stage.")
        }

        if (!mc.isMainThread()) {
            throw IllegalStateException("Writing loaded chunks concurrently is not allowed.")
        }

        this.loadedChunksLock.acquireWrite()
        try {
            this.loadedChunks.remove(chunk.hash)
        } finally {
            this.loadedChunksLock.releaseWrite()
        }
    }

    private fun addLoadedChunk(chunk: Chunk) {
        // add chunk during ChunkStorage is ticking main queues
        if (this.isWriteAllowed.get() && mc.isMainThread()) {
            this.addLoadedChunkMain(chunk)
            return
        }

        this.pendingChunksLock.write {
            this.pendingLoadChunks[chunk.hash] = chunk
        }
    }

    private fun removeLoadedChunk(chunk: Chunk) {
        // remove chunk during ChunkStorage is ticking main queues
        if (this.isWriteAllowed.get() && mc.isMainThread()) {
            this.removeLoadedChunkMain(chunk)
            return
        }

        this.pendingChunksLock.write {
            this.pendingUnloadChunks.add(chunk.hash)
        }
    }

    fun get(x: Int, z: Int): Chunk? {
        val hash = toHash(x, z)

        var retVal: Chunk? = null
        var readLock: Int
        do {
            readLock = this.loadedChunksLock.acquireRead()
            try {
                retVal = this.loadedChunks[hash]
            } catch (ex: Throwable) {
                if (ex is ThreadDeath) {
                    throw ex
                }
                // retry
                continue
            }
        } while (!this.loadedChunksLock.tryReleaseRead(readLock))

        return retVal
    }

    fun isPendingLoaded(x: Int, z: Int): Boolean = this.pendingChunksLock.read {
        this.pendingLoadChunks.containsKey(toHash(x, z))
    }

    fun isLoaded(x: Int, z: Int, checkPending: Boolean = false): Boolean {
        return this.get(x, z)
            ?.run { true }
            ?: run {
                if (checkPending)
                    isPendingLoaded(x, z)
                else false
            }
    }

    // Perform writing jobs
    override fun tickMain() {
        // to be safe...
        while (!this.isWriteAllowed.compareAndSet(false, true)) {
            Thread.onSpinWait()
        }
        // do chunk load/unload batches
        this.pendingChunksLock.write {
            this.loadedChunksLock.acquireWrite()
            this.pendingLoadChunks.forEach { (key, chunk) -> this.loadedChunks[key] = chunk }
            this.pendingUnloadChunks.forEach { this.loadedChunks.remove(it) }
            this.loadedChunksLock.releaseWrite()

            this.pendingLoadChunks.clear()
            this.pendingUnloadChunks.clear()
        }

        super.tickMain()
        this.isWriteAllowed.set(false)
    }

    suspend fun tick() {

    }

}