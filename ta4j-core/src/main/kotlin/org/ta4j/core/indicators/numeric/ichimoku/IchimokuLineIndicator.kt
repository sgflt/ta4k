/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.ichimoku

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.helpers.HighestValueIndicator
import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator
import org.ta4j.core.num.NumFactory

/**
 * An abstract class for Ichimoku clouds indicators.
 *
 * @see [Ichimoku Cloud](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud)
 */
open class IchimokuLineIndicator(
    numFactory: NumFactory,
    private val barCount: Int
) : NumericIndicator(numFactory) {

    /** The period high. */
    private val periodHigh: HighestValueIndicator
    
    /** The period low. */
    private val periodLow: LowestValueIndicator

    init {
        val indicators = Indicators.extended(numFactory)
        periodHigh = HighestValueIndicator(numFactory, indicators.highPrice(), barCount)
        periodLow = LowestValueIndicator(numFactory, indicators.lowPrice(), barCount)
    }

    override val lag: Int = barCount

    override val isStable: Boolean
        get() = periodHigh.isStable && periodLow.isStable

    override fun updateState(bar: Bar) {
        periodHigh.onBar(bar)
        periodLow.onBar(bar)
        
        value = (periodHigh.value + periodLow.value) / numFactory.numOf(2)
    }
}
