/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Variance indicator.
 */
class VarianceIndicator(private val indicator: NumericIndicator, private val barCount: Int) :
    NumericIndicator(indicator.numFactory) {
    private val values = indicator.previous(barCount)
    private var mean = numFactory.zero()
    private var m2 = numFactory.zero() // Sum of squared differences from mean
    private var count = 0

    init {
        require(barCount > 1) { "barCount must be greater than 1" }
    }

    private fun calculate(): Num {
        val newValue = indicator.value
        count++

        // Update mean and M2 using Welford's algorithm
        val delta = newValue - mean
        mean += delta / numFactory.numOf(count)
        val delta2 = newValue - mean
        m2 += delta * delta2

        // Handle sliding window
        if (values.isStable) {
            val oldValue = values.value
            count--

            // Remove the old value from Welford's state
            val deltaSide = oldValue - mean
            mean -= deltaSide / numFactory.numOf(count)
            val delta2Side = oldValue - mean
            m2 -= deltaSide * delta2Side
            // rounding can leave a flat window slightly below zero, which sqrt turns into NaN
            if (m2.isNegative) m2 = numFactory.zero()
        }

        // Calculate sample variance
        return if (count > 1) {
            m2 / numFactory.numOf(count - 1)
        } else {
            numFactory.zero()
        }
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        values.onBar(bar)
        value = calculate()
    }

    override val isStable: Boolean
        get() = count >= barCount && indicator.isStable

    override val lag: Int
        get() = barCount


    override fun toString() = "VAR($indicator, $barCount) => $value"
}
