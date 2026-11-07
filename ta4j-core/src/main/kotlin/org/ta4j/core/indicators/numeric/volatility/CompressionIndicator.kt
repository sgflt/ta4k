/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volatility

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Contraction score on a 0-100 scale; higher means a tighter market.
 *
 * Each width measure (by default ATR %, Bollinger bandwidth and Donchian width %) is turned into an inverted
 * percent rank over [percentileBarCount] bars (`100 - percentRank`), and the three are averaged.
 */
class CompressionIndicator(
    atrPercent: NumericIndicator,
    bollingerWidth: NumericIndicator,
    donchianWidth: NumericIndicator,
    private val percentileBarCount: Int,
) : NumericIndicator(atrPercent.numFactory) {

    init {
        require(percentileBarCount > 0) { "Percentile bar count must be positive, but was: $percentileBarCount" }
    }

    private fun inverted(width: NumericIndicator) = width.percentRank(percentileBarCount).multipliedBy(-1).plus(100)

    /** ATR % compression component. */
    val atrCompression = inverted(atrPercent)

    /** Bollinger bandwidth compression component. */
    val bollingerCompression = inverted(bollingerWidth)

    /** Donchian width compression component. */
    val donchianCompression = inverted(donchianWidth)

    override fun updateState(bar: Bar) {
        atrCompression.onBar(bar)
        bollingerCompression.onBar(bar)
        donchianCompression.onBar(bar)
        value = (atrCompression.value + bollingerCompression.value + donchianCompression.value) / numFactory.three()
    }

    override val lag: Int
        get() = maxOf(atrCompression.lag, bollingerCompression.lag, donchianCompression.lag)

    override val isStable: Boolean
        get() = atrCompression.isStable && bollingerCompression.isStable && donchianCompression.isStable

    override fun toString() = "Compression($percentileBarCount) => $value"
}
