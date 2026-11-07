/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.ichimoku

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator
import org.ta4j.core.num.NumFactory

/**
 * Ichimoku clouds: Chikou Span indicator.
 *
 * @see [Ichimoku Cloud](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud)
 */
class IchimokuChikouSpanIndicator(
    numFactory: NumFactory,
    timeDelay: Int = 26,
) : NumericIndicator(numFactory) {

    private val previousClosePrice =
        PreviousNumericValueIndicator(numFactory, ClosePriceIndicator(numFactory), timeDelay)

    override val lag: Int = timeDelay

    override val isStable: Boolean
        get() = previousClosePrice.isStable

    override fun updateState(bar: Bar) {
        previousClosePrice.onBar(bar)
        value = previousClosePrice.value
    }
}
