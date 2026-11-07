/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Base class for Exponential Moving Average implementations.
 *
 * A NaN input produces NaN; the first valid input after it restarts the average,
 * so a single missing value (e.g. the first bar of a difference) does not poison
 * all later values.
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
        val current = indicator.value
        val previous = previousValue
        val newValue = when {
            // a missing input gives a missing average; the next valid input restarts it
            current.isNaN -> NaN
            previous == null || previous.isNaN -> current
            else -> (current - previous) * multiplier + previous
        }
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
