/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core.num

interface NumFactory {
    /**
     * @return the Num of -1
     */
    fun minusOne(): Num

    /**
     * @return the Num of 0
     */
    fun zero(): Num

    /**
     * @return the Num of 1
     */
    fun one(): Num

    /**
     * @return the Num of 2
     */
    fun two(): Num

    /**
     * @return the Num of 3
     */
    fun three(): Num

    /**
     * @return the Num of 50
     */
    fun fifty(): Num

    /**
     * @return the Num of 100
     */
    fun hundred(): Num

    /**
     * @return the Num of 1000
     */
    fun thousand(): Num

    /**
     * Transforms a [Number] into the [implementation][Num] used by this
     * bar series
     *
     * @param number a [Number] implementing object.
     * @return the corresponding value as a Num implementing object
     */
    fun numOf(number: Number): Num

    /**
     * Transforms a [Number] into the [implementation][Num] used by this
     * bar series
     *
     * @param number as string
     * @return the corresponding value as a Num implementing object
     */
    fun numOf(number: String): Num
}
