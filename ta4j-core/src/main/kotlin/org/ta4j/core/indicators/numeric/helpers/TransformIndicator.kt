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

import java.util.function.UnaryOperator
import kotlin.math.ln
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Transform indicator.
 *
 *
 *
 * Transforms the [Num] of any indicator by using common math operations.
 */
class TransformIndicator(
    private val indicator: NumericIndicator,
    private val transformationFunction: UnaryOperator<Num>,
) : NumericIndicator(indicator.numFactory) {

    override val lag = 0

    private fun calculate() = transformationFunction.apply(indicator.value)

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = indicator.isStable

    override fun toString() = "Transform $indicator WITH $transformationFunction"

    companion object {
        @JvmStatic
        fun plus(indicator: NumericIndicator, coefficient: Number) =
            TransformIndicator(indicator) { it + indicator.numFactory.numOf(coefficient) }

        @JvmStatic
        fun minus(indicator: NumericIndicator, coefficient: Number) =
            TransformIndicator(indicator) { it - indicator.numFactory.numOf(coefficient) }

        @JvmStatic
        fun divide(indicator: NumericIndicator, coefficient: Number) =
            TransformIndicator(indicator) { it / indicator.numFactory.numOf(coefficient) }

        @JvmStatic
        fun multiply(indicator: NumericIndicator, coefficient: Number) =
            TransformIndicator(indicator) { it * indicator.numFactory.numOf(coefficient) }

        @JvmStatic
        fun max(indicator: NumericIndicator, coefficient: Number) =
            TransformIndicator(indicator) { maxOf(it, indicator.numFactory.numOf(coefficient)) }

        @JvmStatic
        fun min(indicator: NumericIndicator, coefficient: Number) =
            TransformIndicator(indicator) { minOf(it, indicator.numFactory.numOf(coefficient)) }

        @JvmStatic
        fun abs(indicator: NumericIndicator) =
            TransformIndicator(indicator) { it.abs() }

        @JvmStatic
        fun pow(indicator: NumericIndicator, coefficient: Number) =
            TransformIndicator(indicator) { it.pow(indicator.numFactory.numOf(coefficient)) }

        @JvmStatic
        fun sqrt(indicator: NumericIndicator) =
            TransformIndicator(indicator) { it.sqrt() }

        @JvmStatic
        fun log(indicator: NumericIndicator) =
            TransformIndicator(indicator) { indicator.numFactory.numOf(ln(it.doubleValue())) }
    }
}
