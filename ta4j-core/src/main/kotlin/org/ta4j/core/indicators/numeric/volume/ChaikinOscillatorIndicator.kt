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
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Chaikin Oscillator indicator.
 *
 * The Chaikin Oscillator is the difference between the 3-day EMA and 10-day EMA
 * of the Accumulation Distribution Line. It oscillates above and below the zero line.
 *
 * @param numFactory the number factory
 * @param shortBarCount the bar count for the short EMA (typically 3)
 * @param longBarCount the bar count for the long EMA (typically 10)
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chaikin_oscillator">
 *      StockCharts - Chaikin Oscillator</a>
 */
class ChaikinOscillatorIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    private val shortBarCount: Int = 3,
    private val longBarCount: Int = 10,
) : NumericIndicator(numFactory) {

    private val accDist = AccumulationDistributionIndicator(numFactory)
    private val emaShort = accDist.ema(shortBarCount)
    private val emaLong = accDist.ema(longBarCount)

    init {
        require(shortBarCount > 0) { "Short bar count must be positive" }
        require(longBarCount > 0) { "Long bar count must be positive" }
        require(shortBarCount < longBarCount) { "Short bar count must be less than long bar count" }
    }

    private fun calculate() = emaShort.value - emaLong.value

    override fun updateState(bar: Bar) {
        emaShort.onBar(bar)
        emaLong.onBar(bar)
        value = calculate()
    }

    override val lag = longBarCount

    override val isStable: Boolean
        get() = emaShort.isStable && emaLong.isStable

    override fun toString(): String {
        return "ChaikinOscillator($shortBarCount, $longBarCount) => $value"
    }
}
