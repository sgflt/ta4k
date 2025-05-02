/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
import org.ta4j.core.indicators.numeric.helpers.TransformIndicator.Companion.multiply
import org.ta4j.core.indicators.numeric.operation.CombineIndicator
import org.ta4j.core.num.Num

/**
 * Hull moving average (HMA) indicator.
 *
 * @see [
 * http://alanhull.com/hull-moving-average](http://alanhull.com/hull-moving-average)
 */
class HMAIndicator(indicator: NumericIndicator, private val barCount: Int) : NumericIndicator(indicator.numFactory) {
    private val sqrtWma: WMAIndicator


    /**
     * Constructor.
     *
     * @param indicator the [Indicator]
     * @param barCount the time frame
     */
    init {
        val halfWma = indicator.wma(barCount / 2)
        val origWma = indicator.wma(barCount)

        val indicatorForSqrtWma = CombineIndicator.minus(multiply(halfWma, 2), origWma)
        sqrtWma = indicatorForSqrtWma.wma(numFactory.numOf(barCount).sqrt().intValue())
    }

    private fun calculate(): Num {
        return sqrtWma.value
    }

    public override fun updateState(bar: Bar) {
        sqrtWma.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = sqrtWma.isStable

    override fun toString() = "HMA($barCount) => $value"
}
