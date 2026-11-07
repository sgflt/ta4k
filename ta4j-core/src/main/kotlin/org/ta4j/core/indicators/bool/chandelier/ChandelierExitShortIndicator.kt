/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.chandelier

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.num.NumFactory

/**
 * The Chandelier Exit (short) Indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chandelier_exit](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chandelier_exit)
 */
class ChandelierExitShortIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    barCount: Int = 22,
    k: Double = 3.0,
) : BooleanIndicator() {
    private val low = Indicators.lowPrice().lowest(barCount)
    private val atr = Indicators.atr(barCount)
    private val k = numFactory.numOf(k)
    private val close = Indicators.closePrice()

    private fun calculate(): Boolean {
        val threshold = low.value + atr.value * k
        return close.isGreaterThan(threshold.doubleValue())
    }

    override fun updateState(bar: Bar) {
        close.onBar(bar)
        low.onBar(bar)
        atr.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = atr.lag

    override val isStable
        get() = close.isStable && low.isStable && atr.isStable
}
