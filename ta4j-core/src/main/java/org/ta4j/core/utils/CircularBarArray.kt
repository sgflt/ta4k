/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2024 Ta4j Organization & respective
 * authors (see AUTHORS)
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
