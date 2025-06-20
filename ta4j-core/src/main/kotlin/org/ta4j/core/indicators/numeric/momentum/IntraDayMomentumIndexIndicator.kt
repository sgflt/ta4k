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
package org.ta4j.core.indicators.numeric.momentum

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.RealBodyIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * IntraDay Momentum Index Indicator.
 *
 * The IntraDay Momentum Index is a measure of the security's strength of trend.
 * It uses the difference between the open and close prices of each bar to
 * calculate momentum.
 *
 * Formula: IMI = (Sum of positive close-open differences) / (Sum of absolute close-open differences) * 100
 *
 * @param numFactory the number factory
 * @param barCount the time frame
 *
 * @see <a href="https://library.tradingtechnologies.com/trade/chrt-ti-intraday-momentum-index.html">
 *      IntraDay Momentum Index</a>
 */
class IntraDayMomentumIndexIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val realBodyIndicator = RealBodyIndicator(numFactory)
    private val window = ArrayDeque<Num>()

    private var positiveSum = numFactory.zero()
    private var absoluteSum = numFactory.zero()

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate(): Num {
        val realBodyValue = realBodyIndicator.value

        // Add new value to window
        window.addLast(realBodyValue)

        // Update sums with new value
        if (realBodyValue.isPositive) {
            positiveSum += realBodyValue
        }
        absoluteSum += realBodyValue.abs()

        // Remove old values if window is full
        if (window.size > barCount) {
            val oldValue = window.removeFirst()
            if (oldValue.isPositive) {
                positiveSum -= oldValue
            }
            absoluteSum -= oldValue.abs()
        }

        // Calculate IMI
        return if (absoluteSum.isZero) {
            numFactory.zero()
        } else {
            (positiveSum / absoluteSum) * numFactory.hundred()
        }
    }

    override fun updateState(bar: Bar) {
        realBodyIndicator.onBar(bar)
        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = window.size >= barCount && realBodyIndicator.isStable

    override fun toString(): String {
        return "IMI($barCount) => $value"
    }
}
