/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.momentum.ATRIndicator

/**
 * MACD weighted by volume and volatility: the difference of a short and a long variable-weighted EMA,
 * `EMA(price * w, n) / EMA(w, n)` with weight `w = volume / ATR(n)`.
 *
 * Unlike [VolatilityNormalizedMACDIndicator], ATR only enters the weights.
 */
class MACDVIndicator(
    private val price: NumericIndicator,
    private val volume: NumericIndicator,
    private val shortBarCount: Int = 12,
    private val longBarCount: Int = 26,
) : AbstractMACDIndicator(price.numFactory) {

    init {
        require(shortBarCount in 1 until longBarCount) {
            "Short bar count must be positive and below long bar count, but was: $shortBarCount, $longBarCount"
        }
    }

    private fun weightedEma(barCount: Int): NumericIndicator {
        val weight = volume.dividedBy(ATRIndicator(numFactory, barCount = barCount))
        return price.multipliedBy(weight).ema(barCount).dividedBy(weight.ema(barCount))
    }

    private val shortAverage = weightedEma(shortBarCount)
    private val longAverage = weightedEma(longBarCount)

    override fun updateState(bar: Bar) {
        shortAverage.onBar(bar)
        longAverage.onBar(bar)
        value = shortAverage.value - longAverage.value
    }

    override val lag = longBarCount

    override val isStable: Boolean
        get() = shortAverage.isStable && longAverage.isStable

    override fun toString() = "MACDV($shortBarCount, $longBarCount) => $value"
}
