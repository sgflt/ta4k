/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Z-score: how many standard deviations an observation is away from its mean.
 *
 * ```
 * z = deviation / standardDeviation
 * ```
 *
 * Both inputs are indicators, so any deviation (e.g. `price - SMA`) and any
 * standard deviation can be used. NaN when the standard deviation is zero or an
 * input is NaN.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Standard_score">Wikipedia: Standard score</a>
 */
class ZScoreIndicator(
    private val deviation: NumericIndicator,
    private val standardDeviation: NumericIndicator,
) : NumericIndicator(deviation.numFactory) {

    private fun calculate(): Num {
        val deviationValue = deviation.value
        val standardDeviationValue = standardDeviation.value
        if (deviationValue.isNaN || standardDeviationValue.isNaN || standardDeviationValue.isZero) {
            return NaN
        }
        return deviationValue / standardDeviationValue
    }

    override fun updateState(bar: Bar) {
        deviation.onBar(bar)
        standardDeviation.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = maxOf(deviation.lag, standardDeviation.lag)

    override val isStable: Boolean
        get() = deviation.isStable && standardDeviation.isStable

    override fun toString() = "ZScore($deviation, $standardDeviation) => $value"
}
