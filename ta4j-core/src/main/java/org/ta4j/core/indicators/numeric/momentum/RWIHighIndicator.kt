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
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * The RandomWalkIndexHighIndicator.
 *
 * Implements the RWI High calculation according to E. Michael Poulos's original methodology.
 * This indicator calculates the maximum relative displacement across multiple timeframes
 * and compares it to what would be expected in a random walk.
 */
class RWIHighIndicator(
    numFactory: NumFactory,
    private val barCount: Int = 14,
) : NumericIndicator(numFactory) {

    private var processedBars = 0
    private val atr = ATRIndicator(numFactory, barCount = barCount)
    private val high = HighPriceIndicator(numFactory)
    private val lowPriceIndicator = LowPriceIndicator(numFactory)

    // Cache of previous low values
    private val previousLows =
        Array(barCount) { i -> PreviousNumericValueIndicator(numFactory, lowPriceIndicator, i + 1) }

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = processedBars > barCount

    private fun calculate(): Num {
        var maxRwi = numFactory.numOf(0)

        // Calculate RWI for each period from minBarCount to maxBarCount
        // and find the maximum value
        for (i in 0 until barCount) {
            val period = i
            val sqrtPeriod = numFactory.numOf(period).sqrt()
            val periodAtr = atr.value  // Ideally, this should be ATR for each specific period

            val highMinusLow = high.value - previousLows[period].value
            val rwiForPeriod = highMinusLow / periodAtr * sqrtPeriod

            if (rwiForPeriod > maxRwi) {
                maxRwi = rwiForPeriod
            }
        }

        return maxRwi
    }

    override fun updateState(bar: Bar) {
        processedBars++

        atr.onBar(bar)
        high.onBar(bar)
        lowPriceIndicator.onBar(bar)
        previousLows.forEach { it.onBar(bar) }

        if (isStable) {
            value = calculate()
        }
    }

    override fun toString(): String {
        return "RWI High($barCount) => $value"
    }
}
