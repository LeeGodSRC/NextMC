package org.fairy.next

import org.fairy.next.extension.log
import org.fairy.next.thread.MainThreadQueue
import org.fairy.next.thread.ParallelBlockingWorker
import org.fairy.next.thread.curThread
import org.fairy.next.thread.newScheduledPool
import java.lang.Exception
import java.util.concurrent.TimeUnit

class UpdateScheduler(val minecraft: NextMinecraft) : MainThreadQueue(), Runnable {

    private val scheduler = newScheduledPool("Tick Update", 1, false)

    lateinit var thread: Thread

    fun start() {
        scheduler.scheduleAtFixedRate(this, 0L, 50L, TimeUnit.MILLISECONDS)
        log.info("Server Started.")
    }

    override fun run() {
        try {
            this.thread = curThread

            // Run Main Thread Queue
            this.tickMain()

            // Tick World Container
            minecraft.worldContainer.tick()
            minecraft.nettyServer.tick()
        } catch (ex: Exception) {
            log.info("fuck")
            log.error(ex)
        }
    }

}