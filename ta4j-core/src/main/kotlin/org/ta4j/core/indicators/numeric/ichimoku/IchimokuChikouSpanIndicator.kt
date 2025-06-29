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
package org.ta4j.core.indicators.numeric.ichimoku

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator
import org.ta4j.core.num.NumFactory

/**
 * Ichimoku clouds: Chikou Span indicator.
 *
 * @see [Ichimoku Cloud](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud)
 */
class IchimokuChikouSpanIndicator(
    numFactory: NumFactory,
    timeDelay: Int = 26,
) : NumericIndicator(numFactory) {

    private val previousClosePrice =
        PreviousNumericValueIndicator(numFactory, ClosePriceIndicator(numFactory), timeDelay)

    override val lag: Int = timeDelay

    override val isStable: Boolean
        get() = previousClosePrice.isStable

    override fun updateState(bar: Bar) {
        previousClosePrice.onBar(bar)
        value = previousClosePrice.value
    }
}
