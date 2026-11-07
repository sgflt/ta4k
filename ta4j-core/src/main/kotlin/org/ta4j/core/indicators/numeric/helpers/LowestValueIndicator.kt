/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import java.util.*
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Lowest value indicator.
 *
 *
 *
 * Returns the lowest indicator value from the bar series within the bar count.
 */
class LowestValueIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator = LowPriceIndicator(numFactory),
    private val barCount: Int,
) : NumericIndicator(numFactory) {
    override val lag = barCount

    /** circular array  */
    private val window: Array<Num?> // TODO CircularNumArray
    private val deque: Deque<Int> = LinkedList()
    private var barsPassed = 0


    /**
     * Constructor.
     *
     * @param indicator the [Indicator]
     * @param barCount the time frame
     */
    init {
        window = arrayOfNulls<Num>(barCount)
    }


    private fun calculate(): Num {
        val actualIndex = barsPassed % barCount

        if (barsPassed >= barCount) {
            val outgoingIndex = (barsPassed - barCount) % barCount
            if (deque.isNotEmpty() && deque.peekFirst() == outgoingIndex) {
                deque.pollFirst()
            }
        }

        val currentValue = indicator.value
        if (currentValue.isNaN) {
            return currentValue
        }

        window[actualIndex] = currentValue

        while (
            deque.isNotEmpty()
            && (window[deque.peekLast()]!! > currentValue
                    || window[deque.peekLast()]!!.isNaN
                    )
        ) {
            deque.pollLast()
        }

        deque.offerLast(actualIndex)
        barsPassed++

        return window[deque.peekFirst()!!]!!
    }


    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = barsPassed >= barCount && indicator.isStable


    override fun toString() = "LoVa($indicator, $barCount) => $value"
}
