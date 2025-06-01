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
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Variance indicator.
 */
class VarianceIndicator(private val indicator: NumericIndicator, private val barCount: Int) :
    NumericIndicator(indicator.numFactory) {
    private val values = indicator.previous(barCount)
    private var mean = numFactory.zero()
    private var m2 = numFactory.zero() // Sum of squared differences from mean
    private var count = 0

    init {
        require(barCount > 1) { "barCount must be greater than 1" }
    }

    private fun calculate(): Num {
        val newValue = indicator.value
        count++

        // Update mean and M2 using Welford's algorithm
        val delta = newValue - mean
        mean += delta / numFactory.numOf(count)
        val delta2 = newValue - mean
        m2 += delta * delta2

        // Handle sliding window
        if (values.isStable) {
            val oldValue = values.value
            count--

            // Remove the old value from Welford's state
            val deltaSide = oldValue - mean
            mean -= deltaSide / numFactory.numOf(count)
            val delta2Side = oldValue - mean
            m2 -= deltaSide * delta2Side
        }

        // Calculate sample variance
        return if (count > 1) {
            m2 / numFactory.numOf(count - 1)
        } else {
            numFactory.zero()
        }
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        values.onBar(bar)
        value = calculate()
    }

    override val isStable: Boolean
        get() = count >= barCount && indicator.isStable

    override val lag: Int
        get() = barCount


    override fun toString() = "VAR($indicator, $barCount) => $value"
}
