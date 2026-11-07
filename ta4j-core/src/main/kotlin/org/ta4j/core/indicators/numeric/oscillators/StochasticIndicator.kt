/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Stochastic of any indicator: `(value - lowest) / (highest - lowest) * 100` over [barCount] bars.
 *
 * Unlike [StochasticOscillatorKIndicator], which compares close to high/low prices, this works on a
 * single series. A flat window keeps the previous value (0 at the start).
 */
class StochasticIndicator(
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(indicator.numFactory) {

    init {
        require(barCount > 0) { "Bar count must be positive, but was: $barCount" }
    }

    private val highest = indicator.highest(barCount)
    private val lowest = indicator.lowest(barCount)
    private val hundred = numFactory.hundred()

    override fun updateState(bar: Bar) {
        highest.onBar(bar)
        lowest.onBar(bar)
        value = calculate()
    }

    private fun calculate(): Num {
        val current = indicator.value
        val high = highest.value
        val low = lowest.value
        if (current.isNaN || high.isNaN || low.isNaN) {
            return NaN
        }
        val range = high - low
        if (range.isZero) {
            return if (value.isNaN) numFactory.zero() else value
        }
        return (current - low) / range * hundred
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = highest.isStable && lowest.isStable

    override fun toString() = "Stochastic($barCount) => $value"
}
