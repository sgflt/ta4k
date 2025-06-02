/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.indicators.numeric.risk

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.utils.CircularNumArray

/**
 * Ulcer Index indicator.
 *
 * The Ulcer Index is a technical indicator that measures downside risk in terms of both
 * depth and duration of price declines. Unlike other volatility measures that consider
 * both upward and downward price movements, the Ulcer Index focuses solely on downside
 * volatility.
 *
 * The calculation involves:
 * 1. For each period in the lookback window, find the running maximum up to that point
 * 2. Calculate the percentage drawdown from that running maximum
 * 3. Square each drawdown percentage
 * 4. Take the average of all squared drawdowns
 * 5. Take the square root of the average
 *
 * @param numFactory the number factory for calculations
 * @param indicator the source indicator (typically close price)
 * @param barCount the time frame (lookback period)
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ulcer_index">
 *      StockCharts - Ulcer Index</a>
 * @see <a href="https://en.wikipedia.org/wiki/Ulcer_index">
 *      Wikipedia - Ulcer Index</a>
 */
class UlcerIndexIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val values = CircularNumArray(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate(): Num {
        if (values.isEmpty || values.isNotFull) {
            return numFactory.zero()
        }

        var sumSquaredDrawdowns = numFactory.zero()
        var runningHigh = numFactory.zero()
        var count = 0

        // Calculate drawdowns for each value in the window
        // Iterate through values in chronological order (oldest to newest)
        for (currentValue in values) {
            // Update running maximum
            if (currentValue > runningHigh) {
                runningHigh = currentValue
            }

            // Calculate percentage drawdown from running high
            val percentageDrawdown = if (runningHigh.isZero) {
                numFactory.zero()
            } else {
                (currentValue - runningHigh) / runningHigh * numFactory.hundred()
            }

            // Add squared drawdown to sum
            sumSquaredDrawdowns += percentageDrawdown.pow(2)
            count++
        }

        // Calculate average squared drawdown and return square root
        val averageSquaredDrawdown = sumSquaredDrawdowns / numFactory.numOf(count)
        return averageSquaredDrawdown.sqrt()
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        values.addLast(indicator.value)
        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = !values.isEmpty && !values.isNotFull && indicator.isStable

    override fun toString(): String {
        return "UlcerIndex($barCount) => $value"
    }
}
