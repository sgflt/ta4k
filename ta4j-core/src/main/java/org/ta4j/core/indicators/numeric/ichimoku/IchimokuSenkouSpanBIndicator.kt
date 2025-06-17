/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.ichimoku

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Ichimoku clouds: Senkou Span B (Leading Span B) indicator.
 *
 * @see [Ichimoku Cloud](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud)
 */
class IchimokuSenkouSpanBIndicator(
    numFactory: NumFactory,
    barCount: Int = 52,
    offset: Int = 26,
) : NumericIndicator(numFactory) {

    /** Ichimoku avg line indicator. */
    private val lineIndicator: IchimokuLineIndicator = IchimokuLineIndicator(numFactory, barCount)

    override val lag: Int = offset + lineIndicator.lag

    override val isStable: Boolean
        get() = lineIndicator.isStable

    override fun updateState(bar: Bar) {
        // Update the underlying indicator
        lineIndicator.onBar(bar)

        // Use the line indicator value directly (it's already averaged)
        // Note: This indicator is forward-shifted by 'offset' periods
        value = lineIndicator.value
    }
}
