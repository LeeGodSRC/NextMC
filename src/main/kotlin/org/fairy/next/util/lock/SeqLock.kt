package org.fairy.next.org.fairy.next.util.lock

/**
 * SeqLocks are used to provide fast locking mechanism to multiple shared variables. A sequential counter
 * (sometimes returned directly by the methods defined in this interface) is used to control access to shared variables.
 *
 *
 *
 * The sequential counter is always initially 0, and it is always odd if a writer holds the lock.
 * In this case, other writers cannot obtain the lock until it is released and readers cannot continue. Readers,
 * when acquiring the read lock, will only read the sequential counter and ensure it is even before continuing.
 * Once reads are complete, the reader will re-check the sequential counter. If the counter has changed since it was
 * initially read, then the read is considered invalid and the reader must re-acquire the read lock and read again.
 *
 *
 *
 *
 * Writers are required to acquire the "write lock" before issuing writes. Writers waiting for the write lock will wait
 * until the lock is released. Writers will only make writes to the shared variable data while the write lock is held.
 * Writers have the option to abort a write, but this can only be used if no writes have occurred to the shared variables.
 * When a write is aborted, the sequential counter is decremented.
 *
 *
 *
 *
 * When SeqLocks are created, they will use a [STORE-STORE][VarHandle.storeStoreFence] fence to prevent re-ordering
 * of writes to shared data that occur before a SeqLock is created.
 *
 *
 *
 *
 * Note that some implementations are not safe for use of multiple writers. See [WeakSeqLock].
 *
 *
 *
 *
 * SeqLocks do not require immediate publishing of writes, only consistency to readers. Implementations, such as
 * [VolatileSeqLock], are free to make such visibility guarantees.
 *
 *
 *
 *
 * Below is an example of a [VolatileSeqLock] used to control access to three variables, r1, r2, and r3.
 *
 *
 *
 *
 * SeqLocks are not re-entrant, and re-entrant calls are undefined (such as acquiring a write/read lock twice).
 * Implementations can offer re-entrant guarantees.
 *
 *
 *
 *
 * Minimum synchronization properties are described by the methods defined by this interface.
 *
 *
 * <pre>
 * class SeqLockUsage {
 *
 * int r1, r2, r3;
 *
 * final VolatileSeqLock seqlock;
 *
 * SeqLockUsage() {
 * r1 = 2; // example default value
 * r2 = 5; // example default value
 * r3 = 6; // example default value
 * // required to be after shared data initialization to prevent re-ordering of writes
 * // note that this does not have to be case if instances of this class are published
 * // on final fields or through other synchronization guaranteeing correct publishing
 * seqlock = new VolatileSeqLock();
 * }
 *
 * // reads and computes a value on r1, r2, and r3
 * int computeValue() {
 * int r1, r2, r3;
 * int lock;
 *
 * do {
 * lock = this.seqlock.acquireRead();
 * r1 = this.r1;
 * r2 = this.r2;
 * r3 = this.r3;
 * } while (!this.seqlock.tryReleaseRead(lock));
 *
 * return r1 * r2 * r3;
 * }
 *
 * void setValues(final int r1, final int r2, final int r3) {
 * this.seqlock.acquireWrite();
 * this.r1 = r1;
 * this.r2 = r2;
 * this.r3 = r3;
 * // try-finally is good practice to use if exceptions can occur during writing.
 * // In this case it is not possible, so it is not used.
 * this.seqlock.releaseWrite();
 * }
 * }
</pre> *
 *
 * @see WeakSeqLock
 */
interface SeqLock {
    /**
     * This function has undefined behaviour if the current thread owns the write lock.
     * This function will also have undefined behaviour if the current implementation does not allow multiple
     * threads to attempt to write and there are multiple threads attempting to acquire this SeqLock.
     *
     *
     * Eventually acquires the write lock. It is guaranteed that the write to the sequential counter
     * will be made with opaque or higher access. The write is also guaranteed to be followed by a [STORE-STORE][VarHandle.storeStoreFence]
     * fence, although it can use a stronger fence, or volatile access for the write.
     *
     */
    fun acquireWrite()

    /**
     * This function has undefined behaviour if the current thread owns the write lock.
     * This function will also have undefined behaviour if the current implementation does not allow multiple
     * threads to attempt to write and there are multiple threads attempting to acquire this SeqLock.
     *
     *
     * Attempts to acquire the read lock. It is guaranteed that the write to the sequential counter, if any,
     * will be made with opaque or higher access. The write, if any, is also guaranteed to be followed by a [STORE-STORE][VarHandle.storeStoreFence]
     * fence, although it can use a stronger fence, or volatile access for the write.
     *
     *
     *
     * There is no guaranteed synchronization to occur if the acquire of the SeqLock fails.
     *
     * @return `true` if the seqlock was acquired, `false` otherwise.
     */
    fun tryAcquireWrite(): Boolean

    /**
     * This function has undefined behaviour if the current thread does not own the write lock.
     *
     *
     * Increments the sequential counter indicating a write has completed. It is guaranteed that the write to the sequential counter
     * is made with opaque or higher access. The write is also guaranteed to be preceded by a [STORE-STORE][VarHandle.storeStoreFence]
     * fence, although it can use a stronger fence, or volatile access for the write.
     *
     */
    fun releaseWrite()

    /**
     * This function has undefined behaviour if the current thread does not own the write lock.
     *
     *
     * Decrements the sequential counter indicating a write has not occurred. It is guaranteed that the write to the sequential counter
     * is made with opaque or higher access. The write is also guaranteed to be preceded by a [STORE-STORE][VarHandle.storeStoreFence]
     * fence, although it can use a stronger fence, or volatile access for the write.
     *
     */
    fun abortWrite()

    /**
     * This function has undefined behaviour if the current thread already owns a read lock.
     *
     *
     * Eventually acquires the read lock and returns an even sequential counter. This function will spinwait until an
     * even sequential counter is read. The counter is required to be read with opaque or higher access. This function
     * is also guaranteed to use at least a [LOAD-LOAD][VarHandle.loadLoadFence] fence after reading the even counter,
     * although it can use a stronger fence, or volatile access for the read.
     *
     * @return An even sequential counter.
     */
    fun acquireRead(): Int

    /**
     * This function has undefined behaviour if the current thread does own a read lock.
     *
     *
     * Checks if the current sequential counter is equal to the specified counter. It is required that the counter is
     * read with opaque or higher access. This function is guaranteed to use at least a [LOAD-LOAD][VarHandle.loadLoadFence]
     * fence before reading the current counter, although it can use a stronger fence, or volatile access for the read.
     *
     * @param read The specified counter.
     * @return `true` if the current sequential counter is equal to the specified counter, `false` otherwise.
     */
    fun tryReleaseRead(read: Int): Boolean

    /**
     * Returns the current sequential counter. It is required that the counter is read with opaque or higher access.
     * This function is guaranteed to use at least a [LOAD-LOAD][VarHandle.loadLoadFence] fence after reading the
     * current counter, although it can use a stronger fence, or volatile access for the read.
     * @return The current sequential counter.
     */
    val sequentialCounter: Int

    /**
     * Checks if the sequential counter is even, which means readers may acquire the read lock.
     * @param read The sequential counter.
     * @return `true` if the counter is even, `false` if the counter is odd.
     */
    fun canRead(read: Int): Boolean {
        return read and 1 == 0
    }
}