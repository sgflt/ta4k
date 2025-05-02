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
package org.ta4j.core.indicators.numeric.channels.keltner

import org.ta4j.core.api.Indicators.atr
import org.ta4j.core.api.series.Bar
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.momentum.ATRIndicator
import org.ta4j.core.num.Num

/**
 * Keltner Channel (lower line) indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:keltner_channels](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:keltner_channels)
 */
class KeltnerChannelLowerIndicator(
    series: BarSeries,
    private val keltnerMiddleIndicator: KeltnerChannelMiddleIndicator,
    private val averageTrueRangeIndicator: ATRIndicator,
    ratio: Double,
) : NumericIndicator(series.numFactory) {
    private val ratio = numFactory.numOf(ratio)


    /**
     * Constructor.
     *
     * @param middle the [.keltnerMiddleIndicator]
     * @param ratio the [.ratio]
     * @param barCountATR the bar count for the [ATRIndicator]
     */
    constructor(
        series: BarSeries,
        middle: KeltnerChannelMiddleIndicator,
        ratio: Double,
        barCountATR: Int,
    ) : this(series, middle, atr(barCountATR), ratio)


    private fun calculate(): Num {
        return keltnerMiddleIndicator.value
            .minus(ratio.multipliedBy(averageTrueRangeIndicator.value))
    }


    public override fun updateState(bar: Bar) {
        keltnerMiddleIndicator.onBar(bar)
        averageTrueRangeIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = keltnerMiddleIndicator.isStable && averageTrueRangeIndicator.isStable

    override val lag: Int
        get() = maxOf(keltnerMiddleIndicator.lag, averageTrueRangeIndicator.lag)
}
