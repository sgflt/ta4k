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
package org.ta4j.core.indicators.numeric.operation

import java.util.function.UnaryOperator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Objects of this class defer the evaluation of a unary operator, like sqrt().
 *
 * There may be other unary operations on Num that could be added here.
 */
class UnaryOperation private constructor(
    private val operator: UnaryOperator<Num>,
    private val operand: NumericIndicator,
) : NumericIndicator(
    operand.numFactory
) {
    private fun calculate(): Num {
        val n = operand.value
        return operator.apply(n)
    }


    public override fun updateState(bar: Bar) {
        operand.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = operand.isStable


    override val lag = 0


    override fun toString() = "UI<$operand> => $value"

    companion object {
        /**
         * Returns an `Indicator` whose value is `√(operand)`.
         *
         * @param operand
         *
         * @return `√(operand)`
         *
         * @see Num.sqrt
         */
        @JvmStatic
        fun sqrt(operand: NumericIndicator) = UnaryOperation({ it.sqrt() }, operand)


        /**
         * Returns an `Indicator` whose value is the absolute value of
         * `operand`.
         *
         * @param operand
         *
         * @return `abs(operand)`
         *
         * @see Num.abs
         */
        @JvmStatic
        fun abs(operand: NumericIndicator) = UnaryOperation({ it.abs() }, operand)

        /**
         * Returns an `Indicator` whose value is `operand^coefficient`.
         *
         * @param operand the operand indicator
         * @param coefficient the power coefficient
         *
         * @return `operand^coefficient`
         *
         * @see Num.pow
         */
        @JvmStatic
        fun pow(operand: NumericIndicator, coefficient: Number): UnaryOperation {
            val numCoefficient = operand.numFactory.numOf(coefficient)
            return UnaryOperation({ it.pow(numCoefficient) }, operand)
        }

        /**
         * Returns an `Indicator` whose value is `log(operand)`.
         *
         * @param operand the operand indicator
         *
         * @return `log(operand)`
         *
         * @apiNote precision may be lost, because this implementation is using the
         * underlying doubleValue method
         */
        @JvmStatic
        fun log(operand: NumericIndicator): UnaryOperation {
            return UnaryOperation({ operand.numFactory.numOf(kotlin.math.ln(it.doubleValue())) }, operand)
        }
    }
}
