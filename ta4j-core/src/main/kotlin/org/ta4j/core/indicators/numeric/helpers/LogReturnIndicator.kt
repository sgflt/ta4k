/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Natural log return of an indicator over [barCount] bars: `ln(x[t] / x[t - barCount])`.
 *
 * NaN during warm-up and when either value is NaN or not positive.
 *
 * @param indicator the source values, e.g. close price
 * @param barCount distance of the compared value in bars
 */
class LogReturnIndicator(
    private val indicator: NumericIndicator,
    private val barCount: Int = 1,
) : NumericIndicator(indicator.numFactory) {

    init {
        require(barCount > 0) { "barCount must be positive, but was: $barCount" }
    }

    private val previousValue = indicator.previous(barCount)

    private fun calculate(): Num {
        val current = indicator.value
        val previous = previousValue.value
        if (current.isNaN || previous.isNaN || !current.isPositive || !previous.isPositive) {
            return NaN
        }
        return (current / previous).log()
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        previousValue.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = previousValue.lag

    override val isStable: Boolean
        get() = previousValue.isStable

    override fun toString() = "LogReturn($barCount) => $value"
}
