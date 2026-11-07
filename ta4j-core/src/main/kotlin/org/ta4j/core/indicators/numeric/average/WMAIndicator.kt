/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.utils.CircularNumArray

/**
 * WMA indicator.
 */
class WMAIndicator(private val indicator: NumericIndicator, private val barCount: Int) :
    NumericIndicator(indicator.numFactory) {
    private val values = CircularNumArray(barCount)
    private var barsPassed = 0


    private fun calculate(): Num {
        if (values.isEmpty) {
            val indicatorValue = indicator.value
            values.addLast(indicatorValue)
            return indicatorValue
        }

        values.addLast(indicator.value)

        var wmaSum = numFactory.zero()
        var i = barCount
        for (v in values.reversed()) {
            wmaSum += v * numFactory.numOf(i--)
        }


        return wmaSum / numFactory.numOf((barCount * (barCount + 1)) / 2)
    }


    public override fun updateState(bar: Bar) {
        ++barsPassed
        indicator.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = barCount


    override val isStable: Boolean
        get() = barsPassed >= barCount && indicator.isStable


    override fun toString() = "WMA($barCount) => $value"
}
