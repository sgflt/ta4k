/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

abstract class RWIIndicator(numFactory: NumFactory, private val barCount: Int) : NumericIndicator(numFactory) {
    private val atr = ATRIndicator(numFactory, barCount = barCount)
    private var processedBars = 0


    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = processedBars > barCount

    protected fun calculate(): Num {
        var maxRwi = numFactory.zero()

        // Calculate RWI for each period from 0 to barCount
        // and find the maximum value
        for (i in 0 until barCount) {
            val period = i
            val sqrtPeriod = numFactory.numOf(period).sqrt()
            val periodAtr = atr.value  // Ideally, this should be ATR for each specific period

            val rwiForPeriod = (getHigh(period) - getLow(period)) / periodAtr * sqrtPeriod
            if (rwiForPeriod > maxRwi) {
                maxRwi = rwiForPeriod
            }
        }

        return maxRwi
    }

    override fun updateState(bar: Bar) {
        atr.onBar(bar)
        processedBars++
    }

    override fun toString(): String {
        return "($barCount) => $value"
    }


    abstract fun getHigh(period: Int): Num
    abstract fun getLow(period: Int): Num
}
