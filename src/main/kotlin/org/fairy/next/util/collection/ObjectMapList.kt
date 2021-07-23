package org.fairy.next.util.collection

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.AbstractReferenceList
import it.unimi.dsi.fastutil.objects.ObjectListIterator
import it.unimi.dsi.fastutil.objects.ObjectSpliterator
import java.util.*
import kotlin.NoSuchElementException


/**
 * list with O(1) remove & contains
 * @author Spottedleaf
 */
open class ObjectMapList<T>(expectedSize: Int = 2, loadFactor: Float = 0.8f) : AbstractReferenceList<T>(), MutableSet<T> {
    private val objectToIndex: Int2IntOpenHashMap = Int2IntOpenHashMap(expectedSize, loadFactor)
    protected var listElements = EMPTY_LIST as Array<T?>
    protected var count = 0

    override val size: Int
        get() = count

    override fun indexOf(element: T): Int {
        return objectToIndex[element.hashCode()]
    }

    override fun lastIndexOf(element: T): Int {
        return super.indexOf(element)
    }

    override fun remove(element: T): Boolean {
        val index = objectToIndex.remove(element.hashCode())
        if (index == Int.MIN_VALUE) {
            return false
        }

        // move the obj at the end to this index
        val endIndex = --count
        val end: T? = listElements[endIndex]
        if (index != endIndex) {
            // not empty after this call
            objectToIndex[end.hashCode()] = index // update index
        }
        listElements[index] = end
        listElements[endIndex] = null
        return true
    }

    override fun add(`object`: T): Boolean {
        val count = count
        val currIndex = objectToIndex.putIfAbsent(`object`.hashCode(), count)
        if (currIndex != Int.MIN_VALUE) {
            return false // already in this list
        }
        var list: Array<T?> = listElements
        if (list.size == count) {
            // resize required
            listElements = list.copyOf(4L.coerceAtLeast(count.toLong() shl 1).toInt())
            list = listElements // overflow results in negative
        }
        list[count] = `object`
        this.count = count + 1
        return true
    }

    override fun add(index: Int, element: T) {
        val currIndex = objectToIndex.putIfAbsent(element.hashCode(), index)
        if (currIndex != Int.MIN_VALUE) {
            return  // already in this list
        }
        val count = count
        var list: Array<T?> = listElements
        if (list.size == count) {
            // resize required
            listElements = list.copyOf(4L.coerceAtLeast(count.toLong() shl 1).toInt())
            list = listElements // overflow results in negative
        }
        System.arraycopy(list, index, list, index + 1, count - index)
        list[index] = element
        this.count = count + 1
    }

    override fun get(index: Int): T? {
        return listElements[index]
    }

    override fun isEmpty(): Boolean {
        return count == 0
    }

    val rawData: Array<T?>
        get() = listElements

    override fun clear() {
        objectToIndex.clear()
        Arrays.fill(listElements, 0, count, null)
        count = 0
    }

    override fun toArray(): Array<Any> {
        return Arrays.copyOf(listElements, count)
    }

    override fun spliterator(): ObjectSpliterator<T> {
        return super<AbstractReferenceList>.spliterator()
    }

    override fun iterator(): ObjectListIterator<T> {
        return Iterator(0)
    }

    private inner class Iterator internal constructor(var current: Int) : ObjectListIterator<T> {
        var lastRet: T? = null
        override fun nextIndex(): Int {
            return current + 1
        }

        override fun previousIndex(): Int {
            return current - 1
        }

        override fun hasNext(): Boolean {
            return current < count
        }

        override fun hasPrevious(): Boolean {
            return current > 0
        }

        override fun next(): T? {
            if (current >= count) {
                throw NoSuchElementException()
            }
            return listElements[current++].also { lastRet = it }
        }

        override fun previous(): T? {
            if (current < 0) {
                throw NoSuchElementException()
            }
            return listElements[--current].also { lastRet = it }
        }

        override fun remove() {
            val lastRet = lastRet ?: throw IllegalStateException()
            this.lastRet = null
            this@ObjectMapList.remove(lastRet)
            --current
        }
    }

    companion object {
        protected val EMPTY_LIST = arrayOfNulls<Any>(0)
    }

    init {
        objectToIndex.defaultReturnValue(Int.MIN_VALUE)
    }
}