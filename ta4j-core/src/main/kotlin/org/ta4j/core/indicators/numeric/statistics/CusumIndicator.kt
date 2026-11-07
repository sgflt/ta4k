/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * One-sided, winsorized cumulative sum (CUSUM) that detects a fall of the value below [targetMean]:
 * ```
 * d(t)     = targetMean - x(t) - allowance
 * S(t)     = max(0, S(t-1) + clip(d(t), ±outlierClipFactor * sigma(t-1)))
 * sigma(t) = scaleDecay * sigma(t-1) + (1 - scaleDecay) * |d(t)|
 * ```
 * The first bar seeds `S = max(0, d)` and `sigma = |d|`. Clipping keeps single outliers from tripping the statistic;
 * only persistent shortfalls raise it. A NaN input keeps the previous state.
 *
 * Feed a negated indicator (or swap the roles of target and value) to watch for upward shifts.
 */
class CusumIndicator(
    private val indicator: NumericIndicator,
    targetMean: Number,
    allowance: Number,
    outlierClipFactor: Number = 3.0,
    scaleDecay: Number = 0.94,
) : NumericIndicator(indicator.numFactory) {

    private val target = numFactory.numOf(targetMean)
    private val slack = numFactory.numOf(allowance)
    private val clipFactor = numFactory.numOf(outlierClipFactor)
    private val decay = numFactory.numOf(scaleDecay)
    private val complement = numFactory.one() - decay
    private var scale: Num = NaN

    init {
        require(!slack.isNegative) { "Allowance must not be negative, but was: $allowance" }
        require(clipFactor.isPositive) { "Outlier clip factor must be positive, but was: $outlierClipFactor" }
        require(decay.isPositive && decay < numFactory.one()) { "Scale decay must be in (0, 1), but was: $scaleDecay" }
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        val x = indicator.value
        if (x.isNaN) return

        val deviation = target - x - slack
        val zero = numFactory.zero()
        if (scale.isNaN) {
            value = if (deviation.isPositive) deviation else zero
            scale = deviation.abs()
            return
        }
        val bound = clipFactor * scale
        val clipped = when {
            deviation > bound -> bound
            deviation < -bound -> -bound
            else -> deviation
        }
        val sum = value + clipped
        value = if (sum.isPositive) sum else zero
        scale = decay * scale + complement * deviation.abs()
    }

    override val lag = 1

    override val isStable: Boolean
        get() = !scale.isNaN && indicator.isStable

    override fun toString() = "Cusum => $value"
}
