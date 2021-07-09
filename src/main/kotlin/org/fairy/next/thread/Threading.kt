package org.fairy.next.thread

import org.fairy.next.constant.IN_IDE
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService

class Threading {

    companion object {
        const val PREFIX = "NextMC - "
        val INSTANCE = Threading()
    }

    private val threadPool : ExecutorService = Executors.newCachedThreadPool(threadFactory("Main Pool"))

    fun newThread(name: String, f : () -> Unit) : Future<*> {
        return threadPool.submit {
            var originalName : String? = null
            if (IN_IDE) {
                originalName = curThread.name
                curThread.name = name
            }

            f.invoke()
            originalName?.let { curThread.name = it }
        }
    }

    fun newThreadPool(name: String, threads: Int) : ExecutorService {
        return if (threads == 1) {
            Executors.newSingleThreadExecutor(threadFactory(name))
        } else {
            Executors.newFixedThreadPool(threads, threadFactory(name))
        }
    }

    fun newScheduledPool(name: String, threads: Int) : ScheduledExecutorService {
        return if (threads == 1) {
            Executors.newSingleThreadScheduledExecutor(threadFactory(name))
        } else {
            Executors.newScheduledThreadPool(threads, threadFactory(name))
        }
    }

}