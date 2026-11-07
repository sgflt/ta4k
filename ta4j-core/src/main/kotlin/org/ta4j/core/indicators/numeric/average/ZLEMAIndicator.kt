/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Zero-lag exponential moving average indicator.
 *
 * @see [
 * http://www.fmlabs.com/reference/default.htm?url=ZeroLagExpMA.htm](http://www.fmlabs.com/reference/default.htm?url=ZeroLagExpMA.htm)
 */
class ZLEMAIndicator(private val indicator: NumericIndicator, private val barCount: Int) :
    NumericIndicator(indicator.numFactory) {
    private val k = numFactory.two() / numFactory.numOf(barCount + 1)
    private val oneMinusK = numFactory.one().minus(k)
    private val lagPreviousValue = indicator.previous((barCount - 1) / 2)
    private var barsPassed = 0


    init {
        require(barCount > 2) { "The bar count must be greater than 2" }
    }


    private fun calculate(): Num {
        if (barsPassed <= lag) {
            return indicator.value
        }

        val zlemaPrev = value
        return k * (numFactory.two() * indicator.value - lagPreviousValue.value) + oneMinusK * zlemaPrev
    }


    override fun updateState(bar: Bar) {
        ++barsPassed
        indicator.onBar(bar)
        lagPreviousValue.onBar(bar)
        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = barsPassed >= lag && lagPreviousValue.isStable && indicator.isStable


    override fun toString() = "ZLEMA($barCount) => $value"
}
