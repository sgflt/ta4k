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
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Loss indicator.
 *
 *
 *
 * Returns the difference of the indicator value of a bar and its previous bar
 * if the indicator value of the current bar is less than the indicator value of
 * the previous bar (otherwise, [NumFactory.zero] is returned).
 */
class LossIndicator(private val indicator: NumericIndicator) : NumericIndicator(indicator.numFactory) {
    override val lag: Int
        get() = previousValueIndicator.lag

    private val previousValueIndicator = indicator.previous()


    private fun calculate(): Num {
        if (!previousValueIndicator.isStable) {
            return numFactory.zero()
        }

        val actualValue = indicator.value
        val previousValue = previousValueIndicator.value
        return if (actualValue < previousValue)
            previousValue - actualValue
        else
            numFactory.zero()
    }


    public override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        previousValueIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = indicator.isStable && previousValueIndicator.isStable


    override fun toString() = "LOSS => $value"
}
