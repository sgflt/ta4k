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

import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

internal class CircularNumArray(capacity: Int) : CircularArray<Num>(capacity, NaN) {

    override fun reversed(): Iterable<Num> {
        return Iterable {
            object : Iterator<Num> {
                private val startIndex: Int = this@CircularNumArray.currentIndex
                private var processed: Int = 0

                override fun hasNext(): Boolean {
                    val index = startIndex - processed
                    return processed < capacity() && index >= 0 && !get(index)!!.isNaN
                }

                override fun next(): Num {
                    val index = startIndex - processed
                    processed++
                    return get(index)!!
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
                return processed < capacity() && !get(nextIndex)!!.isNaN
            }


            override fun next(): Num {
                currentIndex = (currentIndex + 1) % capacity()
                processed++
                return get(currentIndex)!!
            }
        }
    }
}
