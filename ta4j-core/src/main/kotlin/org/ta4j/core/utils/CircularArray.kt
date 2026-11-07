/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.utils

/**
 * A circular array can hold up to N elements. N is known as the capacity of the
 * array. All elements are null initially.
 *
 * A circular array has no bounds. The get() and set() methods apply a modulo
 * operation
 *
 *
 * index % capacity
 *
 *
 * This ensures all get/set operations with index >= 0 are successful.
 *
 *
 * Objects of this class can provide the basis for a FIFO (first in, first out)
 * container. The client that implements such a FIFO is responsible for making
 * sure elements are accessed in a FIFO way.
 *
 * @param <T> the type of element held in the array
</T> */
internal abstract class CircularArray<T> protected constructor(private val capacity: Int, defaultValue: T?) :
    Iterable<T?> {
    protected val elements: ArrayList<T?> = ArrayList(capacity)
    var currentIndex: Int = -1
        private set


    init {
        (0 until capacity).forEach { _ ->
            elements.add(defaultValue)
        }
    }


    fun capacity(): Int {
        return capacity
    }


    fun size(): Int {
        return elements.size
    }


    open operator fun get(index: Int): T? {
        return elements[getIndex(index)]
    }


    protected fun getIndex(index: Int): Int {
        return index % capacity()
    }


    val first: T?
        get() = elements[getIndex(currentIndex + capacity + 1)]


    fun addLast(element: T) {
        ++currentIndex
        elements[getIndex(currentIndex)] = element
    }


    override fun toString(): String {
        return elements.toString()
    }


    abstract fun reversed(): Iterable<T?>?


    val isEmpty: Boolean
        get() = currentIndex == -1

    val isNotFull: Boolean
        get() = currentIndex < capacity - 1
}
