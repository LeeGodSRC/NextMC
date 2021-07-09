package org.fairy.next.org.fairy.next.util.lock

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle


/**
 * SeqLock implementation offering the bare minimum required by the [SeqLock] specification.
 * WeakSeqLocks cannot be used concurrently with multiple writer threads. As such, [.acquireWrite] has
 * the same effect as calling [.tryAcquireWrite]. Writes are not guaranteed to be published immediately, and
 * loads can be re-ordered across write lock handling.
 * @author Spottedleaf
 */
class WeakSeqLock : SeqLock {
    protected var lock = 0
    protected val lockPlain: Int
        protected get() = LOCK_HANDLE.get(this) as Int
    protected var lockOpaque: Int
        protected get() = LOCK_HANDLE.getOpaque(this) as Int
        protected set(value) {
            LOCK_HANDLE.setOpaque(this, value)
        }

    /**
     * {@inheritDoc}
     */
    override fun acquireWrite() {
        val lock = lockPlain
        lockOpaque = lock + 1
        VarHandle.storeStoreFence()
    }

    /**
     * {@inheritDoc}
     */
    override fun tryAcquireWrite(): Boolean {
        acquireWrite()
        return true
    }

    /**
     * {@inheritDoc}
     */
    override fun releaseWrite() {
        val lock = lockPlain
        VarHandle.storeStoreFence()
        lockOpaque = lock + 1
    }

    /**
     * {@inheritDoc}
     */
    override fun abortWrite() {
        val lock = lockPlain
        VarHandle.storeStoreFence()
        lockOpaque = lock xor 1
    }

    /**
     * {@inheritDoc}
     */
    override fun acquireRead(): Int {
        var failures = 0
        var curr: Int
        curr = lockOpaque
        while (!canRead(curr)) {
            for (i in 0 until failures) {
                Thread.onSpinWait()
            }
            if (++failures > 5000) { /* TODO determine a threshold */
                Thread.yield()
            }
            curr = lockOpaque
        }
        VarHandle.loadLoadFence()
        return curr
    }

    /**
     * {@inheritDoc}
     */
    override fun tryReleaseRead(read: Int): Boolean {
        VarHandle.loadLoadFence()
        return lockOpaque == read
    }

    /**
     * {@inheritDoc}
     */
    override val sequentialCounter: Int
        get() {
            val lock = lockOpaque
            VarHandle.loadLoadFence()
            return lock
        }

    companion object {
        protected val LOCK_HANDLE: VarHandle =
            getVarHandle(WeakSeqLock::class.java, "lock", Int::class.javaPrimitiveType)

        fun getVarHandle(lookIn: Class<*>?, fieldName: String?, fieldType: Class<*>?): VarHandle {
            return try {
                MethodHandles.privateLookupIn(lookIn, MethodHandles.lookup()).findVarHandle(lookIn, fieldName, fieldType)
            } catch (ex: Exception) {
                throw RuntimeException(ex) // unreachable
            }
        }
    }

    init {
        VarHandle.storeStoreFence()
    }

}