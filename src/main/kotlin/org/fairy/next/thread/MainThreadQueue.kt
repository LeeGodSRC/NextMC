package org.fairy.next.thread

import org.fairy.next.extension.log
import java.util.concurrent.ConcurrentLinkedQueue

open class MainThreadQueue {

    private val mainThreadQueue = ConcurrentLinkedQueue<() -> Unit>()

    open fun tickMain() {
        var f: (() -> Unit)? = null
        while (this.mainThreadQueue.poll()?.let { f = it; true } == true) {
            f!!.invoke()
        }
    }

    fun queueMain(f: () -> Unit) {
        this.mainThreadQueue.add { f.invoke() }
    }

}