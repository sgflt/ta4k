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
package org.ta4j.core.indicators.numeric.momentum.adx

import org.ta4j.core.api.Indicators.atr
import org.ta4j.core.api.Indicators.minusDMI
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * -DI indicator.
 *
 *
 *
 * Part of the Directional Movement System.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx)
 *
 * @see [https://www.investopedia.com/terms/a/adx.asp](https://www.investopedia.com/terms/a/adx.asp)
 */
class MinusDIIndicator(numFactory: NumFactory, private val barCount: Int) : NumericIndicator(numFactory) {
    private val atrIndicator = atr(barCount)
    private val avgMinusDMIndicator = minusDMI().mma(barCount)


    private fun calculate() = (avgMinusDMIndicator.value / atrIndicator.value) * numFactory.hundred()


    override fun updateState(bar: Bar) {
        atrIndicator.onBar(bar)
        avgMinusDMIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = atrIndicator.isStable && avgMinusDMIndicator.isStable

    override val lag = barCount

    override fun toString() = "MDII($barCount) => $value"
}
