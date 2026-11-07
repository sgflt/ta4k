/*
 * SPDX-License-Identifier: MIT
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
