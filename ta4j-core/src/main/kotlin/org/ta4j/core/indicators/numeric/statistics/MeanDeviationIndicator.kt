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
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.utils.CircularNumArray

/**
 * Mean Deviation indicator (also known as Mean Absolute Deviation).
 *
 * The Mean Deviation measures the average absolute deviation of data points from their mean.
 * It's a measure of variability or dispersion in a dataset.
 *
 * Formula: MD = (1/n) * Σ|xi - mean|
 *
 * Where:
 * - n is the number of periods (barCount)
 * - xi is each individual value
 * - mean is the arithmetic mean of the values
 *
 * This implementation uses streaming algorithms for numerical stability:
 * - Maintains a sliding window using CircularNumArray
 * - Calculates mean incrementally to avoid recalculation
 * - Updates mean deviation incrementally as new values arrive
 *
 * @param indicator the input indicator
 * @param barCount the time frame (number of periods)
 *
 * @see <a href="https://en.wikipedia.org/wiki/Average_absolute_deviation">
 *      Average Absolute Deviation</a>
 */
class MeanDeviationIndicator(
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(indicator.numFactory) {

    private val values = CircularNumArray(barCount)
    private val runningTotal = indicator.runningTotal(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive, but was: $barCount" }
    }

    private fun calculate(): Num {
        if (values.isEmpty || values.isNotFull || !runningTotal.isStable) {
            return NaN
        }

        // Calculate mean using the running total
        val mean = runningTotal.value / numFactory.numOf(barCount)

        // Calculate sum of absolute deviations from mean
        var sumOfAbsoluteDeviations = numFactory.zero()
        for (value in values) {
            value.let {
                sumOfAbsoluteDeviations += (it - mean).abs()
            }
        }

        // Return mean of absolute deviations
        return sumOfAbsoluteDeviations / numFactory.numOf(barCount)
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        runningTotal.onBar(bar)

        val newValue = indicator.value

        // Update values window for deviation calculation
        values.addLast(newValue)

        value = calculate()
    }

    override val lag = maxOf(barCount, runningTotal.lag)

    override val isStable: Boolean
        get() = !values.isEmpty && !values.isNotFull && indicator.isStable && runningTotal.isStable

    override fun toString(): String {
        return "MeanDeviation($barCount) => $value"
    }
}
