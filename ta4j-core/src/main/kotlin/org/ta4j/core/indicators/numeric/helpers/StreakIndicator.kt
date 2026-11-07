/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Length of the current run of rising or falling values.
 *
 * Positive (1, 2, 3, ...) while values rise, negative (-1, -2, ...) while they fall,
 * zero when a value is unchanged and on the first bar. For values
 * `[10, 11, 12, 11, 10]` the streak is `[0, 1, 2, -1, -2]`. NaN when a value is NaN.
 *
 * @param indicator the source values
 */
class StreakIndicator(private val indicator: NumericIndicator) : NumericIndicator(indicator.numFactory) {

    private var previous: Num? = null
    private var streak = 0
    private var hasCompared = false

    private fun calculate(current: Num): Num {
        val previousValue = previous
        previous = current
        if (current.isNaN || previousValue == null || previousValue.isNaN) {
            streak = 0
            hasCompared = false
            return if (current.isNaN) NaN else numFactory.zero()
        }

        hasCompared = true
        streak = when {
            current > previousValue -> if (streak > 0) streak + 1 else 1
            current < previousValue -> if (streak < 0) streak - 1 else -1
            else -> 0
        }
        return numFactory.numOf(streak)
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = calculate(indicator.value)
    }

    override val lag = 2

    override val isStable: Boolean
        get() = indicator.isStable && hasCompared

    override fun toString() = "Streak => $value"
}
