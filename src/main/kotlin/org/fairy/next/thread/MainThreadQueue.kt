package org.fairy.next.org.fairy.next.thread

import java.util.concurrent.ConcurrentLinkedQueue

open class MainThreadQueue {

    private val mainThreadQueue = ConcurrentLinkedQueue<() -> Unit>()

    open fun tickMain() {
        var f: () -> Unit
        while (this.mainThreadQueue.poll().let { f = it; it != null })
            f.invoke()
    }

    fun queueMain(f: () -> Unit) {
        this.mainThreadQueue.add { f.invoke() }
    }

}