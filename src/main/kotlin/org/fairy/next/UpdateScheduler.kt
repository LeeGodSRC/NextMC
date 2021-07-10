package org.fairy.next

import org.fairy.next.org.fairy.next.thread.MainThreadQueue
import org.fairy.next.org.fairy.next.thread.ParallelBlockingWorker
import org.fairy.next.thread.curThread
import org.fairy.next.thread.newScheduledPool
import java.util.concurrent.TimeUnit

class UpdateScheduler(val minecraft: NextMinecraft) : MainThreadQueue(), Runnable {

    private val scheduler = newScheduledPool("Tick Update", 1)

    private val connectionParallelWorker = ParallelBlockingWorker("Connection", 4)

    lateinit var thread: Thread

    fun start() {
        scheduler.scheduleAtFixedRate(this, 0L, 50L, TimeUnit.MILLISECONDS)
    }

    override fun run() {
        this.thread = curThread

        // Run Main Thread Queue
        this.tickMain()

        // Tick World Container
        minecraft.worldContainer.tick()

        this.tickConnection()
    }

    fun tickConnection() {
        this.connectionParallelWorker.runBlocking()
    }

}