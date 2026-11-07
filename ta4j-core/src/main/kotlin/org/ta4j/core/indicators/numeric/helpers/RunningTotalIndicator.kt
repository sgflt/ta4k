/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Running Total aka Cumulative Sum indicator
 *
 * @see [https://en.wikipedia.org/wiki/Running_total](https://en.wikipedia.org/wiki/Running_total)
 */
class RunningTotalIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(
    numFactory
) {
    override val lag = barCount

    private val previousValue = indicator.previous(barCount)
    private var previousSum = numFactory.zero()
    private var processedBars = 0


    private fun calculate(): Num {
        val indicatorValue = indicator.value
        if (indicatorValue.isNaN) {
            return value
        } else {
            ++processedBars
        }

        var sum = previousSum + indicatorValue

        // a NaN leaving the window was never added
        if (previousValue.isStable && !previousValue.value.isNaN) {
            sum -= previousValue.value
        }

        previousSum = sum
        return sum
    }


    override fun toString(): String {
        return javaClass.simpleName + " barCount: " + barCount
    }


    public override fun updateState(bar: Bar) {
        previousValue.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = processedBars >= barCount && indicator.isStable
}
