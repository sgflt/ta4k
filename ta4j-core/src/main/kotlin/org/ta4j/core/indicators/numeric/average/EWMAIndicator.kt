/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Exponentially weighted moving average with an explicit decay factor:
 * `previous * decayFactor + current * (1 - decayFactor)`.
 *
 * Unlike [EMAIndicator], the weight is not derived from [barCount]; [barCount] only sets the warm-up.
 *
 * @param decayFactor weight of the previous average, in (0, 1)
 */
class EWMAIndicator(
    indicator: NumericIndicator,
    barCount: Int,
    private val decayFactor: Double,
) : AbstractEMAIndicator(indicator, barCount, 1.0 - requireDecay(decayFactor)) {

    override fun toString() = "EWMA($barCount, $decayFactor) => $value"

    private companion object {
        fun requireDecay(decayFactor: Double): Double {
            require(decayFactor > 0.0 && decayFactor < 1.0) { "Decay factor must be in (0, 1), but was: $decayFactor" }
            return decayFactor
        }
    }
}
