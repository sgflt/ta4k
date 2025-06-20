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
package org.ta4j.core.indicators.numeric.candles

import org.ta4j.core.api.series.Bar
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.indicators.SeriesRelatedBooleanIndicator

/**
 * Bearish Harami pattern indicator.
 *
 * @see [
 * http://www.investopedia.com/terms/b/bullishharami.asp](http://www.investopedia.com/terms/b/bullishharami.asp)
 */
class BullishHaramiIndicator(series: BarSeries) : SeriesRelatedBooleanIndicator(series) {
    private var previousBar: Bar? = null


    protected fun calculate(bar: Bar): Boolean {
        if (previousBar == null) {
            previousBar = bar
            // Harami is a 2-candle pattern
            return false
        }

        val prevBar = previousBar!!
        previousBar = bar

        if (prevBar.isBearish && bar.isBullish) {
            val prevOpenPrice = prevBar.openPrice
            val prevClosePrice = prevBar.closePrice
            val currOpenPrice = bar.openPrice
            val currClosePrice = bar.closePrice
            return currOpenPrice < prevOpenPrice
                    && currOpenPrice > prevClosePrice
                    && currClosePrice < prevOpenPrice
                    && currClosePrice > prevClosePrice
        }

        return false
    }


    public override fun updateState(bar: Bar) {
        value = calculate(bar)
    }

    override val lag = 2


    override val isStable
        get() = previousBar != null
}
