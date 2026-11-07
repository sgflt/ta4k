/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.utils

import org.ta4j.core.api.series.Bar

internal class CircularBarArray(capacity: Int) : CircularArray<Bar?>(capacity, null) {
    override fun reversed(): Iterable<Bar> {
        return Iterable {
            object : Iterator<Bar> {
                val currentIndex: Int = this@CircularBarArray.currentIndex
                var processed: Int = 0


                override fun hasNext(): Boolean {
                    return processed < capacity() && index > -1 && get(index - 1) != null
                }


                val index: Int
                    get() = currentIndex - processed


                override fun next(): Bar {
                    val num = get(index)
                    ++processed
                    return num!!
                }
            }
        }
    }


    override fun iterator(): Iterator<Bar> {
        return object : Iterator<Bar> {
            var currentIndex: Int = this@CircularBarArray.currentIndex
            var processed: Int = 0


            override fun hasNext(): Boolean {
                return processed < capacity()
            }


            override fun next(): Bar {
                ++currentIndex
                ++processed
                return get(currentIndex)!!
            }
        }
    }
}
