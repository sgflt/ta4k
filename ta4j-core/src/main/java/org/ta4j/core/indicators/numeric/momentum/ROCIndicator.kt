/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Rate of change (ROCIndicator) indicator (also called "Momentum").
 *
 * The ROCIndicator calculation compares the current value with the value "n" periods ago.
 *
 * @see <a href="https://www.investopedia.com/terms/p/pricerateofchange.asp">ROC</a>
 */
class ROCIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(numFactory) {
    private val nPeriodsAgo = PreviousNumericValueIndicator(numFactory, indicator, barCount)
    private var processedBars = 0

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = processedBars > barCount

    private fun calculate(): Num {
        return (indicator.value - nPeriodsAgo.value) / nPeriodsAgo.value * numFactory.hundred()
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        nPeriodsAgo.onBar(bar)
        value = calculate()
        processedBars++
    }
}
