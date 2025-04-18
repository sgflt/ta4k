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
package org.ta4j.core.indicators.numeric.oscilators

import org.ta4j.core.api.Indicators.awesomeOscillator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Acceleration-deceleration indicator.
 */
class AccelerationDecelerationIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    shortBarCount: Int = 5,
    longBarCount: Int = 34,
) : NumericIndicator(numFactory) {
    private val awesome = awesomeOscillator(shortBarCount, longBarCount)
    private val sma = awesome.sma(shortBarCount)


    private fun calculate(): Num = awesome.value.minus(sma.value)


    public override fun updateState(bar: Bar) {
        awesome.onBar(bar)
        sma.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = awesome.isStable && sma.isStable

    override val lag: Int
        get() = awesome.lag
}
