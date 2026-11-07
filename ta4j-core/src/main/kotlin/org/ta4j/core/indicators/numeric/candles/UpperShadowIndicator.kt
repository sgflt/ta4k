/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Upper shadow height indicator.
 *
 * Provides the (absolute) difference between the high price and the highest
 * price of the candle body. I.e.: high price - max(open price, close price)
 *
 * @see [Candlestick Formation](http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks#formation)
 */
class UpperShadowIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {

    override fun updateState(bar: Bar) {
        val openPrice = bar.openPrice
        val closePrice = bar.closePrice

        value = if (closePrice > openPrice) {
            // Bullish candle
            bar.highPrice - closePrice
        } else {
            // Bearish candle
            bar.highPrice - openPrice
        }
    }

    override val lag: Int = 0
}
