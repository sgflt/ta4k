/**
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
package org.ta4j.core.indicators.ichimoku

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.helpers.HighestValueIndicator
import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator
import org.ta4j.core.num.NumFactory

/**
 * An abstract class for Ichimoku clouds indicators.
 *
 * @see [Ichimoku Cloud](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud)
 */
open class IchimokuLineIndicator(
    numFactory: NumFactory,
    private val barCount: Int
) : NumericIndicator(numFactory) {

    /** The period high. */
    private val periodHigh: HighestValueIndicator
    
    /** The period low. */
    private val periodLow: LowestValueIndicator

    init {
        val indicators = Indicators.extended(numFactory)
        periodHigh = HighestValueIndicator(numFactory, indicators.highPrice(), barCount)
        periodLow = LowestValueIndicator(numFactory, indicators.lowPrice(), barCount)
    }

    override val lag: Int = barCount

    override val isStable: Boolean
        get() = periodHigh.isStable && periodLow.isStable

    override fun updateState(bar: Bar) {
        periodHigh.onBar(bar)
        periodLow.onBar(bar)
        
        value = (periodHigh.value + periodLow.value) / numFactory.numOf(2)
    }
}