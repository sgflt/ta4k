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

package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Moving average convergence divergence (MACD) indicator (also called
 * "MACD Absolute Price Oscillator (APO)").
 *
 * @param numFactory the number factory
 * @param indicator the source indicator
 * @param shortBarCount the short time frame (normally 12)
 * @param longBarCount the long time frame (normally 26)
 *
 * @see <a href=
 *      "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:moving_average_convergence_divergence_macd">
 *      http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:moving_average_convergence_divergence_macd</a>
 */
class MACDIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val shortBarCount: Int = 12,
    private val longBarCount: Int = 26,
) : NumericIndicator(numFactory) {

    val shortTermEma = indicator.ema(shortBarCount)
    val longTermEma = indicator.ema(longBarCount)

    init {
        require(shortBarCount < longBarCount) {
            "Long term period count must be greater than short term period count"
        }
    }

    private fun calculate() = shortTermEma.value - longTermEma.value

    override fun updateState(bar: Bar) {
        shortTermEma.onBar(bar)
        longTermEma.onBar(bar)
        value = calculate()
    }

    override val lag = longBarCount

    override val isStable: Boolean
        get() = shortTermEma.isStable && longTermEma.isStable

    override fun toString(): String {
        return "MACD($shortBarCount, $longBarCount) => $value"
    }
}
