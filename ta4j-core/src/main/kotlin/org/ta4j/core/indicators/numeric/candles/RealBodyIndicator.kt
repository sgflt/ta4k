/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Real (candle) body height indicator.
 *
 *
 *
 * Provides the (relative) difference between the open price and the close price
 * of a bar. I.e.: close price - open price
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks.formation](http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks.formation)
 */
class RealBodyIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {
    private fun calculate(bar: Bar) = bar.closePrice.minus(bar.openPrice)

    public override fun updateState(bar: Bar) {
        value = calculate(bar)
    }

    override val lag: Int = 0
}
