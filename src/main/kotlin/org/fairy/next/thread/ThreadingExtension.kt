package org.fairy.next.thread

import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory

val curThread : Thread
    get() = Thread.currentThread()

fun threadFactory(name: String, daemon: Boolean = true) : ThreadFactory {
    return ThreadFactoryBuilder()
        .setNameFormat(Threading.PREFIX + name)
        .setDaemon(daemon)
        .build()
}

fun newThread(name: String, f : () -> Unit) : Future<*> {
    return Threading.INSTANCE.newThread(name, f)
}

fun newThreadPool(name: String, threads: Int) : ExecutorService {
    return Threading.INSTANCE.newThreadPool(name, threads)
}

fun newScheduledPool(name: String, threads: Int) : ScheduledExecutorService {
    return Threading.INSTANCE.newScheduledPool(name, threads)
}

fun newScheduledPool(name: String, threads: Int, daemon: Boolean = true) : ScheduledExecutorService {
    return Threading.INSTANCE.newScheduledPool(name, threads, daemon)
}