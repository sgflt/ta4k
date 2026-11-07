/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.ichimoku

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Ichimoku clouds: Senkou Span B (Leading Span B) indicator.
 *
 * @see [Ichimoku Cloud](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud)
 */
class IchimokuSenkouSpanBIndicator(
    numFactory: NumFactory,
    barCount: Int = 52,
    offset: Int = 26,
) : NumericIndicator(numFactory) {

    /** Ichimoku avg line indicator. */
    private val lineIndicator: IchimokuLineIndicator = IchimokuLineIndicator(numFactory, barCount)

    override val lag: Int = offset + lineIndicator.lag

    override val isStable: Boolean
        get() = lineIndicator.isStable

    override fun updateState(bar: Bar) {
        // Update the underlying indicator
        lineIndicator.onBar(bar)

        // Use the line indicator value directly (it's already averaged)
        // Note: This indicator is forward-shifted by 'offset' periods
        value = lineIndicator.value
    }
}
