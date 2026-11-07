/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.chandelier

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.num.NumFactory

/**
 * The Chandelier Exit (long) Indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chandelier_exit](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chandelier_exit)
 */
class ChandelierExitLongIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    barCount: Int = 22,
    k: Double = 3.0,
) : BooleanIndicator() {
    private val high = Indicators.highPrice().highest(barCount)
    private val atr = Indicators.atr(barCount)
    private val k = numFactory.numOf(k)
    private val close = Indicators.closePrice()

    private fun calculate(): Boolean = close.isLessThan(high.value.minus(atr.value.times(k)).doubleValue())

    override fun updateState(bar: Bar) {
        close.onBar(bar)
        high.onBar(bar)
        atr.onBar(bar)
        value = calculate()
    }

    override val lag
        get() = atr.lag

    override val isStable
        get() = close.isStable && high.isStable && atr.isStable
}
