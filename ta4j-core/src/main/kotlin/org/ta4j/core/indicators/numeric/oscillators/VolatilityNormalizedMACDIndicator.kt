/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN

/**
 * Volatility-normalized MACD (Alex Spiroglou's MACD-V): `(EMA(fast) - EMA(slow)) / ATR * scale`.
 *
 * Normalizing by ATR makes readings comparable across instruments and time; see [momentumState] for the usual
 * ±50 / ±150 zones.
 *
 * @param atr the average true range, usually `atr(26)`
 *
 * @see <a href="https://chartschool.stockcharts.com/table-of-contents/technical-indicators-and-overlays/technical-indicators/macd-v">StockCharts</a>
 */
class VolatilityNormalizedMACDIndicator(
    price: NumericIndicator,
    private val atr: NumericIndicator,
    private val fastBarCount: Int = 12,
    private val slowBarCount: Int = 26,
    scale: Number = 100,
) : AbstractMACDIndicator(price.numFactory) {

    private val fastEma = price.ema(fastBarCount)
    private val slowEma = price.ema(slowBarCount)
    private val scaleNum = numFactory.numOf(scale)

    init {
        require(fastBarCount in 1 until slowBarCount) {
            "Fast bar count must be positive and below slow bar count, but was: $fastBarCount, $slowBarCount"
        }
    }

    override fun updateState(bar: Bar) {
        fastEma.onBar(bar)
        slowEma.onBar(bar)
        atr.onBar(bar)
        val range = atr.value
        value = if (range.isNaN || range.isZero) NaN else (fastEma.value - slowEma.value) / range * scaleNum
    }

    /** Momentum zone of this MACD-V. */
    fun momentumState(profile: MACDVMomentumProfile = MACDVMomentumProfile()) =
        MACDVMomentumStateIndicator(this, profile)

    override val lag: Int
        get() = maxOf(slowBarCount, atr.lag)

    override val isStable: Boolean
        get() = fastEma.isStable && slowEma.isStable && atr.isStable

    override fun toString() = "MACD-V($fastBarCount, $slowBarCount) => $value"
}
