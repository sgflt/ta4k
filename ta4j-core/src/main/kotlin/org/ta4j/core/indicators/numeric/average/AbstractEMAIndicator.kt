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
 * Base class for Exponential Moving Average implementations.
 */
abstract class AbstractEMAIndicator(
    private val indicator: NumericIndicator,
    protected val barCount: Int,
    multiplier: Double,
) : NumericIndicator(indicator.numFactory) {
    private val multiplier = numFactory.numOf(multiplier)

    private var previousValue: Num? = null
    private var barsPassed = 0

    override val lag = barCount

    private fun calculate(): Num {
        if (previousValue == null) {
            previousValue = indicator.value
            return previousValue!!
        }

        val newValue = (indicator.value - previousValue!!) * multiplier + previousValue!!
        previousValue = newValue
        return newValue
    }


    override fun toString(): String {
        return javaClass.simpleName + " barCount: " + barCount
    }


    override val isStable: Boolean
        get() = barsPassed >= barCount && indicator.isStable


    override fun updateState(bar: Bar) {
        ++barsPassed
        indicator.onBar(bar)
        value = calculate()
    }
}
