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
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Distance From Moving Average indicator.
 *
 * Calculates the percentage distance of the current close price from a moving average:
 * (close - MA) / MA
 *
 * This indicator is useful for determining how far the current price has deviated
 * from its moving average in percentage terms. Positive values indicate the price
 * is above the moving average, while negative values indicate it's below.
 *
 * The indicator accepts any NumericIndicator as the moving average, providing
 * flexibility to use SMA, EMA, WMA, or any other moving average type.
 *
 * @param movingAverage the moving average indicator to compare against
 *
 * @see <a href="https://school.stockcharts.com/doku.php?id=technical_indicators:distance_from_ma">
 *      Distance From Moving Average - StockCharts</a>
 */
class DistanceFromMAIndicator(
    numFactory: NumFactory,
    private val movingAverage: NumericIndicator,
) : NumericIndicator(numFactory) {

    private fun calculate(bar: Bar): Num {
        val close = bar.closePrice
        val ma = movingAverage.value
        return if (ma.isZero) {
            numFactory.zero()
        } else {
            (close - ma) / ma
        }
    }

    override fun updateState(bar: Bar) {
        movingAverage.onBar(bar)
        value = calculate(bar)
    }

    override val lag = movingAverage.lag

    override val isStable
        get() = movingAverage.isStable

    override fun toString(): String {
        return "DistanceFromMA($movingAverage) => $value"
    }
}
