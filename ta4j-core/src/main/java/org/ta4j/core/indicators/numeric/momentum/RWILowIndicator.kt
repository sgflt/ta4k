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
class RWILowIndicator(
    numFactory: NumFactory,
    private val barCount: Int = 14,
) : RWIIndicator(numFactory, barCount) {

    private val lowPrice = LowPriceIndicator(numFactory)
    private val highPrice = HighPriceIndicator(numFactory)

    // Cache of previous low values
    private val previsouHighs =
        Array(barCount) { i -> PreviousNumericValueIndicator(numFactory, highPrice, i + 1) }

    override fun getHigh(period: Int): Num {
        return previsouHighs[period].value
    }

    override fun getLow(period: Int): Num {
        return lowPrice.value
    }

    override fun updateState(bar: Bar) {
        super.updateState(bar)
        lowPrice.onBar(bar)
        highPrice.onBar(bar)
        previsouHighs.forEach { it.onBar(bar) }

        if (isStable) {
            value = calculate()
        }
    }

    override fun toString(): String {
        return "RWI Low ${super.toString()}"
    }
}
