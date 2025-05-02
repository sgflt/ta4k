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
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.SMAIndicator
import org.ta4j.core.indicators.numeric.statistics.StandardDeviationIndicator
import org.ta4j.core.num.Num

/**
 * %B indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:bollinger_band_perce](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:bollinger_band_perce)
 */
class PercentBIndicator(private val indicator: NumericIndicator, barCount: Int, k: Double) :
    NumericIndicator(indicator.numFactory) {
    private val bbu: BollingerBandsUpperIndicator
    private val bbl: BollingerBandsLowerIndicator


    /**
     * Constructor.
     *
     * @param indicator the [Indicator] (usually `ClosePriceIndicator`)
     * @param barCount the time frame
     * @param k the K multiplier (usually 2.0)
     */
    init {
        val bbm = BollingerBandsMiddleIndicator(SMAIndicator(indicator, barCount))
        val sd = StandardDeviationIndicator(indicator, barCount)
        bbu = BollingerBandsUpperIndicator(bbm, sd, numFactory.numOf(k))
        bbl = BollingerBandsLowerIndicator(bbm, sd, numFactory.numOf(k))
    }

    private fun calculate(): Num {
        val value = indicator.value
        val upValue = bbu.value
        val lowValue = bbl.value
        return value.minus(lowValue).dividedBy(upValue.minus(lowValue))
    }

    public override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        bbl.onBar(bar)
        bbu.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = indicator.isStable && bbl.isStable && bbu.isStable

    override fun toString() = "%%Bi(${indicator}) => $value"

    override val lag: Int
        get() = maxOf(indicator.lag, bbl.lag, bbu.lag)
}
