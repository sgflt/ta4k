/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
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
import org.ta4j.core.num.NumFactory

/**
 * Triangular Moving Average (TMA) Indicator.
 *
 * TMA is a double-smoothing of the Simple Moving Average (SMA).
 *
 * Triangular Moving Average (TMA) is a type of moving average that places
 * greater emphasis on the central portion of the data series. It is calculated
 * as a second smoothing of a Simple Moving Average (SMA), making it smoother
 * than the SMA or Exponential Moving Average (EMA). Due to its double-smoothing
 * nature, the TMA is commonly used to identify long-term trends in financial
 * markets, reducing noise from short-term fluctuations.
 *
 * @property indicator an indicator
 * @property barCount the Simple Moving Average time frame
 */
class TMAIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int
) : NumericIndicator(numFactory) {

    private var processedBars = 0
    private val firstSMA = indicator.sma(barCount)
    private val secondSMA = firstSMA.sma(barCount)

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        firstSMA.onBar(bar)
        secondSMA.onBar(bar)
        processedBars++
        value = secondSMA.value
    }

    override val lag: Int
        get() = barCount * 2  // Double smoothing means double lag

    override val isStable: Boolean
        get() = processedBars >= (barCount * 2 - 1)

    override fun toString(): String {
        return "TMA($barCount) => $value"
    }
}
