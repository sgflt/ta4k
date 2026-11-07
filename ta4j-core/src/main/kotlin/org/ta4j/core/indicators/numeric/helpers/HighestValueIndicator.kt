/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Highest value indicator.
 *
 *
 *
 * Returns the highest indicator value from the bar series within the bar count.
 */
class HighestValueIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator = HighPriceIndicator(numFactory),
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    override val lag = barCount

    /** circular array  */
    private val window = arrayOfNulls<Num>(barCount)
    private val deque = ArrayDeque<Int>()
    private var barsPassed = 0


    private fun calculate(): Num {
        val actualIndex = barsPassed % barCount

        if (barsPassed >= barCount) {
            val outgoingIndex = (barsPassed - barCount) % barCount
            if (deque.isNotEmpty() && deque.first() == outgoingIndex) {
                deque.removeFirst()
            }
        }

        val currentValue = indicator.value
        window[actualIndex] = currentValue

        while (deque.isNotEmpty() && (window[deque.last()]!! < currentValue
                    || window[deque.last()]!!.isNaN
                    )
        ) {
            deque.removeLast()
        }

        deque.addLast(actualIndex)
        barsPassed++

        return window[deque.first()]!!
    }


    public override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = barsPassed >= barCount && indicator.isStable


    override fun toString() = "HiVa($indicator, $barCount) => $value"
}
