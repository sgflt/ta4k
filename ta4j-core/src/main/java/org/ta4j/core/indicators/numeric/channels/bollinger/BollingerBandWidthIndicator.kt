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

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Bollinger BandWidth indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:bollinger_band_width](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:bollinger_band_width)
 */
class BollingerBandWidthIndicator
/**
 * Constructor.
 *
 * @param bbu the upper band Indicator.
 * @param bbm the middle band Indicator. Typically an `SMAIndicator` is
 * used.
 * @param bbl the lower band Indicator.
 */(
    private val bbu: BollingerBandsUpperIndicator,
    private val bbm: BollingerBandsMiddleIndicator,
    private val bbl: BollingerBandsLowerIndicator,
) : NumericIndicator(bbm.numFactory) {
    private fun calculate() = bbu.value
        .minus(bbl.value)
        .dividedBy(bbm.value)
        .multipliedBy(numFactory.hundred())

    override fun updateState(bar: Bar) {
        bbl.onBar(bar)
        bbm.onBar(bar)
        bbu.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = bbl.isStable && bbu.isStable && bbm.isStable

    override val lag: Int
        get() = maxOf(bbl.lag, bbu.lag, bbm.lag)

    override fun toString() = "BolBaWidth(${bbu}, ${bbm}, ${bbl}) => $value"
}
