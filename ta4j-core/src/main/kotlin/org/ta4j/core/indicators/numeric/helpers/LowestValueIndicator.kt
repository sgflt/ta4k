/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

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
    private val deque = ArrayDeque<Int>()
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
            if (deque.isNotEmpty() && deque.first() == outgoingIndex) {
                deque.removeFirst()
            }
        }

        val currentValue = indicator.value
        if (currentValue.isNaN) {
            return currentValue
        }

        window[actualIndex] = currentValue

        while (
            deque.isNotEmpty()
            && (window[deque.last()]!! > currentValue
                    || window[deque.last()]!!.isNaN
                    )
        ) {
            deque.removeLast()
        }

        deque.addLast(actualIndex)
        barsPassed++

        return window[deque.first()]!!
    }


    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = barsPassed >= barCount && indicator.isStable


    override fun toString() = "LoVa($indicator, $barCount) => $value"
}
