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
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Ichimoku clouds: Senkou Span A (Leading Span A) indicator.
 *
 * @see [Ichimoku Cloud](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud)
 */
class IchimokuSenkouSpanAIndicator(
    numFactory: NumFactory,
    tenkanSenBarCount: Int = 9,
    kijunSenBarCount: Int = 26,
    private val conversionLine: IchimokuTenkanSenIndicator = IchimokuTenkanSenIndicator(numFactory, tenkanSenBarCount),
    private val baseLine: IchimokuKijunSenIndicator = IchimokuKijunSenIndicator(numFactory, kijunSenBarCount),
    offset: Int = 26,
) : NumericIndicator(numFactory) {

    override val lag: Int = offset + maxOf(conversionLine.lag, baseLine.lag)

    override val isStable: Boolean
        get() = conversionLine.isStable && baseLine.isStable

    private fun calculate(): Num = if (conversionLine.value == NaN || baseLine.value == NaN) {
        NaN
    } else {
        (conversionLine.value + baseLine.value) / numFactory.two()
    }

    override fun updateState(bar: Bar) {
        // Update the underlying indicators
        conversionLine.onBar(bar)
        baseLine.onBar(bar)

        // Calculate the average of conversion line and base line
        // Note: This indicator is forward-shifted by 'offset' periods
        value = calculate()
    }
}
