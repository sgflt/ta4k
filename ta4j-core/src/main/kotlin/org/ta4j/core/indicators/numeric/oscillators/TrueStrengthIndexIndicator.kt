/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN

/**
 * True Strength Index (William Blau).
 *
 * TSI = 100 * EMA(EMA(change, first), second) / EMA(EMA(|change|, first), second)
 *
 * Ranges from -100 to 100. NaN while the absolute change is zero (flat prices).
 *
 * @see <a href="https://school.stockcharts.com/doku.php?id=technical_indicators:true_strength_index">StockCharts</a>
 */
class TrueStrengthIndexIndicator(
    indicator: NumericIndicator,
    private val firstSmoothingPeriod: Int = 25,
    private val secondSmoothingPeriod: Int = 13,
) : NumericIndicator(indicator.numFactory) {

    init {
        require(firstSmoothingPeriod > 0) { "First smoothing period must be positive, but was: $firstSmoothingPeriod" }
        require(secondSmoothingPeriod > 0) { "Second smoothing period must be positive, but was: $secondSmoothingPeriod" }
    }

    private val change = indicator.difference()
    private val smoothedChange = change.ema(firstSmoothingPeriod).ema(secondSmoothingPeriod)
    private val smoothedAbsoluteChange = change.abs().ema(firstSmoothingPeriod).ema(secondSmoothingPeriod)
    private val hundred = numFactory.hundred()

    override fun updateState(bar: Bar) {
        smoothedChange.onBar(bar)
        smoothedAbsoluteChange.onBar(bar)
        val numerator = smoothedChange.value
        val denominator = smoothedAbsoluteChange.value
        value = if (numerator.isNaN || denominator.isNaN || denominator.isZero) {
            NaN
        } else {
            numerator / denominator * hundred
        }
    }

    override val lag = firstSmoothingPeriod + secondSmoothingPeriod + 1

    override val isStable: Boolean
        get() = smoothedChange.isStable && smoothedAbsoluteChange.isStable

    override fun toString() = "TSI($firstSmoothingPeriod, $secondSmoothingPeriod) => $value"
}
