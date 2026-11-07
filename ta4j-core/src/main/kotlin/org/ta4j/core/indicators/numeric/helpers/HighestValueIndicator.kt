/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import java.util.*
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
    private val deque = LinkedList<Int>()
    private var barsPassed = 0


    private fun calculate(): Num {
        val actualIndex = barsPassed % barCount

        if (barsPassed >= barCount) {
            val outgoingIndex = (barsPassed - barCount) % barCount
            if (deque.isNotEmpty() && deque.peekFirst() == outgoingIndex) {
                deque.pollFirst()
            }
        }

        val currentValue = indicator.value
        window[actualIndex] = currentValue

        while (deque.isNotEmpty() && (window[deque.peekLast()]!! < currentValue
                    || window[deque.peekLast()]!!.isNaN
                    )
        ) {
            deque.pollLast()
        }

        deque.offerLast(actualIndex)
        barsPassed++

        return window[deque.peekFirst()!!]!!
    }


    public override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = barsPassed >= barCount && indicator.isStable


    override fun toString() = "HiVa($indicator, $barCount) => $value"
}
