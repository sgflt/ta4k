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
 * The Moving volume weighted average price (MVWAP) Indicator.
 *
 * MVWAP is a simple moving average applied to VWAP values over a specified period.
 * This creates a smoother version of VWAP that reduces noise while maintaining
 * the volume-weighted characteristics.
 *
 * @param numFactory the number factory
 * @param vwapBarCount the time frame for VWAP calculation
 * @param mvwapBarCount the time frame for the moving average of VWAP
 *
 * @see <a href=
 *      "http://www.investopedia.com/articles/trading/11/trading-with-vwap-mvwap.asp">
 *      http://www.investopedia.com/articles/trading/11/trading-with-vwap-mvwap.asp</a>
 */
class MVWAPIndicator(
    numFactory: NumFactory,
    vwapBarCount: Int,
    private val mvwapBarCount: Int,
) : NumericIndicator(numFactory) {

    private val sma = VWAPIndicator(numFactory, vwapBarCount).sma(mvwapBarCount)

    init {
        require(mvwapBarCount > 0) { "MVWAP bar count must be positive" }
    }

    private fun calculate() = sma.value

    override fun updateState(bar: Bar) {
        sma.onBar(bar)
        value = calculate()
    }

    override val lag = sma.lag

    override val isStable: Boolean
        get() = sma.isStable

    override fun toString(): String {
        return "MVWAP($mvwapBarCount) => $value"
    }
}
