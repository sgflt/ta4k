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
import org.ta4j.core.indicators.numeric.average.MMAIndicator
import org.ta4j.core.indicators.numeric.candles.TRIndicator
import org.ta4j.core.num.NumFactory

/**
 * Average true range indicator.
 */
class ATRIndicator(
    numFactory: NumFactory,
    tr: NumericIndicator = TRIndicator(numFactory),
    barCount: Int,
) :
    NumericIndicator(numFactory) {
    private val averageTrueRangeIndicator = MMAIndicator(tr, barCount)

    init {
        require(barCount > 0) { "barCount must be positive" }
    }

    override val lag: Int
        get() = averageTrueRangeIndicator.lag

    override fun updateState(bar: Bar) {
        averageTrueRangeIndicator.onBar(bar)
        value = calculate()
    }

    private fun calculate() = averageTrueRangeIndicator.value

    override val isStable: Boolean
        get() = averageTrueRangeIndicator.isStable

    override fun toString() = "ATR($averageTrueRangeIndicator) => $value"
}
