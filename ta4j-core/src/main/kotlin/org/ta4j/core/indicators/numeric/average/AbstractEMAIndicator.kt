/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Base class for Exponential Moving Average implementations.
 */
abstract class AbstractEMAIndicator(
    private val indicator: NumericIndicator,
    protected val barCount: Int,
    multiplier: Double,
) : NumericIndicator(indicator.numFactory) {
    private val multiplier = numFactory.numOf(multiplier)

    private var previousValue: Num? = null
    private var barsPassed = 0

    override val lag = barCount

    private fun calculate(): Num {
        if (previousValue == null) {
            previousValue = indicator.value
            return previousValue!!
        }

        val newValue = (indicator.value - previousValue!!) * multiplier + previousValue!!
        previousValue = newValue
        return newValue
    }


    override fun toString(): String {
        return javaClass.simpleName + " barCount: " + barCount
    }


    override val isStable: Boolean
        get() = barsPassed >= barCount && indicator.isStable


    override fun updateState(bar: Bar) {
        ++barsPassed
        indicator.onBar(bar)
        value = calculate()
    }
}
