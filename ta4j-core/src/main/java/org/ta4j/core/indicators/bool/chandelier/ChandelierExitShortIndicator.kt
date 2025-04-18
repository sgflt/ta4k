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
package org.ta4j.core.indicators.bool.chandelier

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.num.NumFactory

/**
 * The Chandelier Exit (short) Indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chandelier_exit](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chandelier_exit)
 */
class ChandelierExitShortIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    barCount: Int = 22,
    k: Double = 3.0,
) : BooleanIndicator() {
    private val low = Indicators.lowPrice().lowest(barCount)
    private val atr = Indicators.atr(barCount)
    private val k = numFactory.numOf(k)
    private val close = Indicators.closePrice()

    private fun calculate(): Boolean = close.isGreaterThan(low.value.plus(atr.value.multipliedBy(k)).doubleValue())

    override fun updateState(bar: Bar) {
        close.onBar(bar)
        low.onBar(bar)
        atr.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = atr.lag

    override val isStable
        get() = close.isStable && low.isStable && atr.isStable
}
