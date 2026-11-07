/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Lower shadow height indicator.
 *
 *
 *
 * Provides the (absolute) difference between the low price and the lowest price
 * of the candle body. I.e.: low price - min(open price, close price)
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks.formation](http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks.formation)
 */
class LowerShadowIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {
    private fun calculate(bar: Bar): Num {
        val openPrice = bar.openPrice
        val closePrice = bar.closePrice

        if (closePrice > openPrice) {
            // Bullish
            return openPrice.minus(bar.lowPrice)
        }

        // Bearish
        return closePrice.minus(bar.lowPrice)
    }


    override fun updateState(bar: Bar) {
        value = calculate(bar)
    }

    override val lag: Int = 0
}
