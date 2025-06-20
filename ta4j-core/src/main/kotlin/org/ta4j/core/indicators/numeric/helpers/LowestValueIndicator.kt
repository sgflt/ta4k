/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
