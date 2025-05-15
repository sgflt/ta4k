/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
import org.ta4j.core.num.Num

/**
 * Zero-lag exponential moving average indicator.
 *
 * @see [
 * http://www.fmlabs.com/reference/default.htm?url=ZeroLagExpMA.htm](http://www.fmlabs.com/reference/default.htm?url=ZeroLagExpMA.htm)
 */
class ZLEMAIndicator(private val indicator: NumericIndicator, private val barCount: Int) :
    NumericIndicator(indicator.numFactory) {
    private val k = numFactory.two() / numFactory.numOf(barCount + 1)
    private val oneMinusK = numFactory.one().minus(k)
    private val lagPreviousValue = indicator.previous((barCount - 1) / 2)
    private var barsPassed = 0


    init {
        require(barCount > 2) { "The bar count must be greater than 2" }
    }


    private fun calculate(): Num {
        if (barsPassed <= lag) {
            return indicator.value
        }

        val zlemaPrev = value
        return k * (numFactory.two() * indicator.value - lagPreviousValue.value) + oneMinusK * zlemaPrev
    }


    override fun updateState(bar: Bar) {
        ++barsPassed
        indicator.onBar(bar)
        lagPreviousValue.onBar(bar)
        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = barsPassed >= lag && lagPreviousValue.isStable && indicator.isStable


    override fun toString() = "ZLEMA($barCount) => $value"
}
