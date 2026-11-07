/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Buying pressure: close - min(low, previous close).
 *
 * On the first bar there is no previous close, so it is close - low (as [TRIndicator][org.ta4j.core.indicators.numeric.candles.TRIndicator]
 * uses high - low there).
 */
class BuyingPressureIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {
    private var previousClose: Num? = null

    override fun updateState(bar: Bar) {
        val previous = previousClose
        val trueLow = if (previous == null || bar.lowPrice < previous) bar.lowPrice else previous
        value = bar.closePrice - trueLow
        previousClose = bar.closePrice
    }

    override val lag = 2

    override val isStable: Boolean
        get() = previousClose != null

    override fun toString() = "BuyingPressure => $value"
}
