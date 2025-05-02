/*
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
package org.ta4j.core.indicators.helpers.previous

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import java.util.*

/**
 * Returns the (n-th) previous value of an indicator.
 */
internal class PreviousValueHelper<T>(private val indicator: Indicator<T>, private val n: Int) : Indicator<T?> {
    private val previousValues = LinkedList<T>()
    override var value: T? = null
        private set
    override val lag = n
    private var currentBar: Bar? = null

    /**
     * Constructor.
     *
     * @param indicator the indicator from which to calculate the previous value
     * @param n parameter defines the previous n-th value
     */
    init {
        require(n >= 1) { "n must be positive number, but was: " + n }
    }

    private fun calculate(): T? {
        val currentValue = indicator.value
        previousValues.addLast(currentValue)

        if (previousValues.size > n) {
            return previousValues.removeFirst()
        }

        return null
    }

    override fun toString() = "PREV($n, $indicator) => $value"

    override fun onBar(bar: Bar) {
        if (bar !== currentBar) {
            updateState(bar)
            currentBar = bar
        }
    }

    fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = previousValues.size == n && value != null
}
