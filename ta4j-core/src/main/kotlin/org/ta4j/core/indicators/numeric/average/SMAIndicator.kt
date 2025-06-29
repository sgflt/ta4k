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
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Simple moving average (SMA) indicator.
 *
 * @see [https://www.investopedia.com/terms/s/sma.asp](https://www.investopedia.com/terms/s/sma.asp)
 */
class SMAIndicator(indicator: NumericIndicator, private val barCount: Int) : NumericIndicator(indicator.numFactory) {
    private val sum = indicator.runningTotal(barCount)
    private var processedBars = 0
    private val divisor = numFactory.numOf(barCount)

    private fun calculate(): Num {
        val sum = partialSum()
        return sum / divisor
    }

    private fun partialSum() = sum.value

    override val lag: Int
        get() = barCount

    override val isStable
        get() = processedBars >= barCount && sum.isStable

    public override fun updateState(bar: Bar) {
        ++processedBars
        sum.onBar(bar)
        value = calculate()
    }

    override fun toString() = "SMA($barCount) => $value"
}
