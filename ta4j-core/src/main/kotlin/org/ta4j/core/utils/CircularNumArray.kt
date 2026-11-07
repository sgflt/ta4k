/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.utils

import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

internal class CircularNumArray(capacity: Int) : CircularArray<Num>(capacity, NaN) {

    override operator fun get(index: Int): Num {
        return elements[getIndex(index)]!!
    }

    override fun reversed(): Iterable<Num> {
        return Iterable {
            object : Iterator<Num> {
                private val startIndex: Int = this@CircularNumArray.currentIndex
                private var processed: Int = 0

                override fun hasNext(): Boolean {
                    val index = startIndex - processed
                    return processed < capacity() && index >= 0 && !get(index).isNaN
                }

                override fun next(): Num {
                    val index = startIndex - processed
                    processed++
                    return get(index)
                }
            }
        }
    }


    override fun iterator(): Iterator<Num> {
        return object : Iterator<Num> {
            private var currentIndex: Int = this@CircularNumArray.currentIndex
            private var processed: Int = 0

            override fun hasNext(): Boolean {
                val nextIndex = (currentIndex + 1) % capacity()
                return processed < capacity() && !get(nextIndex).isNaN
            }


            override fun next(): Num {
                currentIndex = (currentIndex + 1) % capacity()
                processed++
                return get(currentIndex)
            }
        }
    }
}
