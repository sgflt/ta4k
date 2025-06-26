/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Smoothed Moving Average (SMMA) Indicator.
 *
 * Smoothed Moving Average (SMMA) is a type of moving average that applies
 * exponential smoothing over a longer period. It is designed to emphasize the
 * overall trend by minimizing the impact of short-term fluctuations. Unlike the
 * Exponential Moving Average (EMA), which assigns more weight to recent prices,
 * the SMMA evenly distributes the influence of older data while still applying
 * some smoothing.
 *
 * SMMA_1 = SMA_n
 * SMMA_t = (SMMA_t-1 * (n - 1) + Price_t) / n
 *
 * @property indicator an indicator
 * @property barCount the Simple Moving Average time frame
 */
class SMMAIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private lateinit var previousSMMA: Num
    private var processedBars = 0
    private val values = mutableListOf<Num>()

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        processedBars++

        val currentPrice = indicator.value

        if (processedBars == 1) {
            calculateInitialDataPoint(currentPrice)
        } else if (processedBars <= barCount) {
            // Accumulate values for initial SMA calculation
            values.addLast(currentPrice)
            calculateWithinWarmUpPhase()
        } else {
            calculate(currentPrice)
        }
    }

    private fun calculate(currentPrice: Num) {
        // SMMA formula: SMMA_t = (SMMA_t-1 * (n - 1) + Price_t) / n
        value = (previousSMMA * numFactory.numOf(barCount - 1) + currentPrice) / numFactory.numOf(barCount)
        previousSMMA = value
    }

    private fun calculateWithinWarmUpPhase() {
        if (processedBars == barCount) {
            // Calculate initial SMA as the first SMMA
            var sum = numFactory.zero()
            for (v in values) {
                sum += v
            }
            value = sum / numFactory.numOf(barCount)
            previousSMMA = value
            values.clear() // No longer needed
        } else {
            // While accumulating, return simple average
            var sum = numFactory.zero()
            for (v in values) {
                sum += v
            }
            value = sum / numFactory.numOf(processedBars)
        }
    }

    private fun calculateInitialDataPoint(currentPrice: Num) {
        // The first SMMA value is the first data point
        value = currentPrice
        previousSMMA = currentPrice
        values.addLast(currentPrice)
    }

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = processedBars >= barCount

    override fun toString(): String {
        return "SMMAIndicator barCount: $barCount => $value"
    }
}
