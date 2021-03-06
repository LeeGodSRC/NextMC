package org.fairy.next.thread

import org.fairy.next.extension.log
import org.fairy.next.thread.newThreadPool
import java.util.*
import java.util.concurrent.CountDownLatch

class ParallelBlockingWorker(val name: String, val threads: Int) {

    private val executorService = newThreadPool(name, threads - 1)

    // It will block main until parallel done
    // List must not be modified during it
    fun <T> runBlocking(items: List<T>, f: (T, Int) -> Unit) {
        val latch = CountDownLatch(threads)

        repeat(threads) {
            val task = {
                for (i in it..items.lastIndex step threads) {
                    if (i > items.lastIndex) {
                        break
                    }
                    f.invoke(items[i], i)
                }

                latch.countDown()
            }

            if (it == 0) {
                task.invoke()
            } else {
                this.executorService.submit(task)
            }
        }

        latch.await()
    }

}