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
 * Objects of this class defer evaluation of an arithmetic operation.
 *
 *
 *
 * This is a lightweight version of the
 * [CombineIndicator];
 * it doesn't cache.
 */
class BinaryOperation private constructor(
    private val operator: BinaryOperator<Num>,
    private val left: NumericIndicator,
    private val right: NumericIndicator,
) : NumericIndicator(left.numFactory) {

    private fun calculate() = operator.apply(left.value, right.value)

    override fun updateState(bar: Bar) {
        left.onBar(bar)
        right.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = left.isStable && right.isStable

    override val lag = 0

    override fun toString() = "BI<$left, $right> => $value"

    companion object {
        /**
         * Returns an `Indicator` whose value is `(left + right)`.
         *
         * @param left
         * @param right
         *
         * @return `left + right`, rounded as necessary
         *
         * @see Num.plus
         */
        fun sum(left: NumericIndicator, right: NumericIndicator) = BinaryOperation({ a, b -> a.plus(b) }, left, right)


        /**
         * Returns an `Indicator` whose value is `(left - right)`.
         *
         * @param left
         * @param right
         *
         * @return `left - right`, rounded as necessary
         *
         * @see Num.minus
         */
        fun difference(left: NumericIndicator, right: NumericIndicator) =
            BinaryOperation({ a, b -> a.minus(b) }, left, right)


        /**
         * Returns an `Indicator` whose value is `(left * right)`.
         *
         * @param left
         * @param right
         *
         * @return `left * right`, rounded as necessary
         *
         * @see Num.multipliedBy
         */
        fun product(left: NumericIndicator, right: NumericIndicator) =
            BinaryOperation({ a, b -> a.multipliedBy(b) }, left, right)


        /**
         * Returns an `Indicator` whose value is `(left / right)`.
         *
         * @param left
         * @param right
         *
         * @return `left / right`, rounded as necessary
         *
         * @see Num.dividedBy
         */
        fun quotient(left: NumericIndicator, right: NumericIndicator) =
            BinaryOperation({ a, b -> a.dividedBy(b) }, left, right)


        /**
         * Returns the minimum of `left` and `right` as an
         * `Indicator`.
         *
         * @param left
         * @param right
         *
         * @return the `Indicator` whose value is the smaller of `left` and
         * `right`. If they are equal, `left` is returned.
         *
         * @see Num.min
         */
        fun min(left: NumericIndicator, right: NumericIndicator) = BinaryOperation({ a, b -> a.min(b) }, left, right)


        /**
         * Returns the maximum of `left` and `right` as an
         * `Indicator`.
         *
         * @param left
         * @param right
         *
         * @return the `Indicator` whose value is the greater of `left` and
         * `right`. If they are equal, `left` is returned.
         *
         * @see Num.max
         */
        fun max(left: NumericIndicator, right: NumericIndicator) = BinaryOperation({ a, b -> a.max(b) }, left, right)
    }
}
