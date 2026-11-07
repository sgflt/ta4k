/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator
import org.ta4j.core.indicators.numeric.helpers.HighestValueIndicator
import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * The Stochastic RSI Indicator.
 *
 * Stoch RSI = (RSI - MinimumRSIn) / (MaximumRSIn - MinimumRSIn)
 */
class StochasticRSIIndicator(
    numFactory: NumFactory,
    indicator: NumericIndicator = ClosePriceIndicator(numFactory),
    private val barCount: Int,
    private val rsi: NumericIndicator = RSIIndicator(indicator, barCount),
) : NumericIndicator(numFactory) {
    private val minRsi = LowestValueIndicator(numFactory, rsi, barCount)
    private val maxRsi = HighestValueIndicator(numFactory, rsi, barCount)

    override val lag: Int
        get() = rsi.lag

    override val isStable: Boolean
        get() = rsi.isStable && minRsi.isStable && maxRsi.isStable

    private fun calculate(): Num {
        val divisor = maxRsi.value - minRsi.value
        return if (divisor.isZero) {
            rsi.numFactory.zero()
        } else {
            (rsi.value - minRsi.value) / divisor
        }
    }

    override fun updateState(bar: Bar) {
        rsi.onBar(bar)
        minRsi.onBar(bar)
        maxRsi.onBar(bar)
        value = calculate()
    }
}
