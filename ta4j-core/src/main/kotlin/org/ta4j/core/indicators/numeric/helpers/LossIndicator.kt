/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Loss indicator.
 *
 *
 *
 * Returns the difference of the indicator value of a bar and its previous bar
 * if the indicator value of the current bar is less than the indicator value of
 * the previous bar (otherwise, [NumFactory.zero] is returned).
 */
class LossIndicator(private val indicator: NumericIndicator) : NumericIndicator(indicator.numFactory) {
    override val lag: Int
        get() = previousValueIndicator.lag

    private val previousValueIndicator = indicator.previous()


    private fun calculate(): Num {
        if (!previousValueIndicator.isStable) {
            return numFactory.zero()
        }

        val actualValue = indicator.value
        val previousValue = previousValueIndicator.value
        return if (actualValue < previousValue)
            previousValue - actualValue
        else
            numFactory.zero()
    }


    public override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        previousValueIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = indicator.isStable && previousValueIndicator.isStable


    override fun toString() = "LOSS => $value"
}
