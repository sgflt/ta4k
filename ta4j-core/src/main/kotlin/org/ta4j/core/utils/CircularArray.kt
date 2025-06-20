/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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


    operator fun get(index: Int): T? {
        return elements[getIndex(index)]
    }


    private fun getIndex(index: Int): Int {
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
