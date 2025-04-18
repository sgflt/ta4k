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
package org.ta4j.core.indicators.numeric.operation

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import java.util.function.BinaryOperator

/**
 * Combine indicator.
 *
 * Combines two Num indicators by using common math operations.
 */
class CombineIndicator(
    private val indicatorLeft: NumericIndicator,
    private val indicatorRight: NumericIndicator,
    private val combineFunction: BinaryOperator<Num>,
) : NumericIndicator(indicatorLeft.numFactory) {

    private fun calculate() = combineFunction.apply(indicatorLeft.value, indicatorRight.value)

    public override fun updateState(bar: Bar) {
        indicatorLeft.onBar(bar)
        indicatorRight.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = indicatorLeft.isStable && indicatorRight.isStable

    override val lag = 0

    companion object {
        fun plus(indicatorLeft: NumericIndicator, indicatorRight: NumericIndicator) =
            CombineIndicator(indicatorLeft, indicatorRight) { a, b -> a.plus(b) }

        fun minus(indicatorLeft: NumericIndicator, indicatorRight: NumericIndicator) =
            CombineIndicator(indicatorLeft, indicatorRight) { a, b -> a.minus(b) }

        fun divide(indicatorLeft: NumericIndicator, indicatorRight: NumericIndicator) =
            CombineIndicator(indicatorLeft, indicatorRight) { a, b -> a.dividedBy(b) }

        fun multiply(indicatorLeft: NumericIndicator, indicatorRight: NumericIndicator) =
            CombineIndicator(indicatorLeft, indicatorRight) { a, b -> a.multipliedBy(b) }

        fun max(indicatorLeft: NumericIndicator, indicatorRight: NumericIndicator) =
            CombineIndicator(indicatorLeft, indicatorRight) { a, b -> a.max(b) }

        fun min(indicatorLeft: NumericIndicator, indicatorRight: NumericIndicator) =
            CombineIndicator(indicatorLeft, indicatorRight) { a, b -> a.min(b) }
    }
}

