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
