/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers.previous

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * @author Lukáš Kvídera
 */
class PreviousNumericValueIndicator(
    numFactory: NumFactory,
    indicator: NumericIndicator,
    n: Int,
) :
    NumericIndicator(numFactory) {
    private val previousValueHelper = PreviousValueHelper(indicator, n)


    private fun calculate(): Num {
        val value = previousValueHelper.value
        return value ?: NaN
    }


    override fun updateState(bar: Bar) {
        previousValueHelper.onBar(bar)
        value = calculate()
    }

    override val lag
        get() = previousValueHelper.lag


    override val isStable: Boolean
        get() = previousValueHelper.isStable && !value.isNaN


    override fun toString(): String {
        return previousValueHelper.toString()
    }
}
