/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import kotlin.math.ln
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Rolling Hurst exponent from a log-variogram regression.
 *
 * For each lag `1..maximumLag` it takes the mean squared difference of values that far apart within the last
 * [barCount] values, regresses `ln(variogram)` on `ln(lag)` and returns half the slope, bounded to `[0, 1]`.
 * Around 0.5 the series behaves like a random walk, above it trends and below it mean-reverts.
 *
 * NaN until the window is full and while it holds a NaN.
 */
class HurstExponentIndicator(
    private val indicator: NumericIndicator,
    private val barCount: Int,
    private val maximumLag: Int = minOf(10, barCount - 1),
) : NumericIndicator(indicator.numFactory) {

    private val window = ArrayDeque<Num>(barCount)
    private var nanValues = 0

    init {
        require(barCount >= 3) { "Bar count must be at least 3, but was: $barCount" }
        require(maximumLag in 2 until barCount) { "Maximum lag must be in 2 until $barCount, but was: $maximumLag" }
    }

    private fun calculate(): Num {
        var sumX = 0.0
        var sumY = 0.0
        var sumXY = 0.0
        var sumXX = 0.0
        for (lag in 1..maximumLag) {
            var total = numFactory.zero()
            for (i in lag until window.size) {
                val difference = window[i] - window[i - lag]
                total += difference * difference
            }
            val variogram = (total / numFactory.numOf(window.size - lag)).doubleValue().coerceAtLeast(VARIOGRAM_FLOOR)
            val x = ln(lag.toDouble())
            val y = ln(variogram)
            sumX += x
            sumY += y
            sumXY += x * y
            sumXX += x * x
        }
        val n = maximumLag.toDouble()
        val slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)
        if (!slope.isFinite()) return NaN
        return numFactory.numOf((slope / 2).coerceIn(0.0, 1.0))
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        if (window.size == barCount && window.removeFirst().isNaN) --nanValues
        window.addLast(indicator.value)
        if (indicator.value.isNaN) ++nanValues
        value = if (window.size < barCount || nanValues > 0) NaN else calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = window.size >= barCount && nanValues == 0 && indicator.isStable

    override fun toString() = "Hurst($barCount, $maximumLag) => $value"

    private companion object {
        const val VARIOGRAM_FLOOR = 1e-12
    }
}
