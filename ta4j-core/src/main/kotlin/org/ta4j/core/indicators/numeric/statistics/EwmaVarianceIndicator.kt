/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Exponentially weighted moving variance (RiskMetrics style):
 * ```
 * variance(t) = decay * variance(t-1) + (1 - decay) * (x(t) - mean(t-1))²
 * mean(t)     = decay * mean(t-1)     + (1 - decay) * x(t)
 * ```
 * Both are seeded with the population mean and variance of the first [barCount] values. A NaN input gives NaN and
 * starts a new seed window.
 */
class EwmaVarianceIndicator(
    private val indicator: NumericIndicator,
    private val barCount: Int,
    private val decayFactor: Double = 0.94,
) : NumericIndicator(indicator.numFactory) {

    private val decay = numFactory.numOf(decayFactor)
    private val complement = numFactory.one() - decay
    private val seed = ArrayList<Num>(barCount)
    private var mean: Num = NaN
    private var variance: Num = NaN

    init {
        require(barCount >= 1) { "Bar count must be positive, but was: $barCount" }
        require(decayFactor > 0 && decayFactor < 1) { "Decay factor must be in (0, 1), but was: $decayFactor" }
    }

    private fun seedValues() {
        val count = numFactory.numOf(seed.size)
        mean = seed.fold(numFactory.zero()) { sum, x -> sum + x } / count
        variance = seed.fold(numFactory.zero()) { sum, x -> sum + (x - mean) * (x - mean) } / count
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        val x = indicator.value
        when {
            x.isNaN -> {
                seed.clear()
                mean = NaN
                variance = NaN
            }

            mean.isNaN -> {
                seed.add(x)
                if (seed.size == barCount) seedValues()
            }

            else -> {
                val deviation = x - mean
                variance = decay * variance + complement * deviation * deviation
                mean = decay * mean + complement * x
            }
        }
        value = variance
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = !variance.isNaN && indicator.isStable

    override fun toString() = "EwmaVariance($barCount, $decayFactor) => $value"
}
