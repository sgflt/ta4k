/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * True range indicator.
 *
 * <pre>
 * TrueRange = MAX(high - low, high - previousClose, previousClose - low)
</pre> *
 */
class TRIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {
    private var previousBar: Bar? = null
    override val lag = 2
    override var isStable = false
        private set


    private fun calculate(bar: Bar): Num {
        val high = bar.highPrice
        val low = bar.lowPrice
        val hl = high.minus(low)

        if (previousBar == null) {
            previousBar = bar
            return hl.abs()
        }

        isStable = true
        val previousClose = previousBar!!.closePrice
        val hc = high - previousClose
        val cl = previousClose - low
        previousBar = bar
        return maxOf(hl.abs(), hc.abs(), cl.abs())
    }


    override fun updateState(bar: Bar) {
        value = calculate(bar)
    }
}
