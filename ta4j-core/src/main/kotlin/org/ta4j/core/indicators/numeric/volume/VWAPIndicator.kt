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

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * The volume-weighted average price (VWAP) Indicator.
 *
 * @param numFactory the number factory
 * @param barCount the time frame (lookback period)
 *
 * @see <a href=
 *      "http://www.investopedia.com/articles/trading/11/trading-with-vwap-mvwap.asp">
 *      http://www.investopedia.com/articles/trading/11/trading-with-vwap-mvwap.asp</a>
 * @see <a href=
 *      "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:vwap_intraday">
 *      http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:vwap_intraday</a>
 * @see <a href="https://en.wikipedia.org/wiki/Volume-weighted_average_price">
 *      https://en.wikipedia.org/wiki/Volume-weighted_average_price</a>
 */
class VWAPIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val typicalPrice = Indicators.extended(numFactory).typicalPrice()
    private val volume = Indicators.extended(numFactory).volume()

    private val typicalPriceVolume = typicalPrice.multipliedBy(volume)
    private val cumulativeTPV = typicalPriceVolume.runningTotal(barCount)
    private val cumulativeVolume = volume.runningTotal(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate() = if (cumulativeVolume.value.isZero) {
        typicalPrice.value
    } else {
        cumulativeTPV.value / cumulativeVolume.value
    }

    override fun updateState(bar: Bar) {
        cumulativeTPV.onBar(bar)
        cumulativeVolume.onBar(bar)

        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = cumulativeTPV.isStable && cumulativeVolume.isStable

    override fun toString(): String {
        return "VWAP($barCount) => $value"
    }
}
