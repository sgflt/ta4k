/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.operation

import java.util.function.BinaryOperator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

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
        @JvmStatic
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
        @JvmStatic
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
        @JvmStatic
        fun product(left: NumericIndicator, right: NumericIndicator) =
            BinaryOperation({ a, b -> a * b }, left, right)


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
        @JvmStatic
        fun quotient(left: NumericIndicator, right: NumericIndicator) =
            BinaryOperation({ a, b -> a / b }, left, right)


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
        @JvmStatic
        fun min(left: NumericIndicator, right: NumericIndicator) = BinaryOperation({ a, b -> minOf(a, b) }, left, right)


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
        @JvmStatic
        fun max(left: NumericIndicator, right: NumericIndicator) = BinaryOperation({ a, b -> maxOf(a, b) }, left, right)

        // Overloaded methods for operations with constants

        /**
         * Returns an `Indicator` whose value is `(indicator + coefficient)`.
         *
         * @param indicator the indicator
         * @param coefficient the coefficient to add
         *
         * @return `indicator + coefficient`, rounded as necessary
         */
        @JvmStatic
        fun sum(indicator: NumericIndicator, coefficient: Number): BinaryOperation {
            val constantIndicator = ConstantNumericIndicator(indicator.numFactory.numOf(coefficient))
            return BinaryOperation({ a, b -> a.plus(b) }, indicator, constantIndicator)
        }

        /**
         * Returns an `Indicator` whose value is `(indicator - coefficient)`.
         *
         * @param indicator the indicator
         * @param coefficient the coefficient to subtract
         *
         * @return `indicator - coefficient`, rounded as necessary
         */
        @JvmStatic
        fun difference(indicator: NumericIndicator, coefficient: Number): BinaryOperation {
            val constantIndicator = ConstantNumericIndicator(indicator.numFactory.numOf(coefficient))
            return BinaryOperation({ a, b -> a.minus(b) }, indicator, constantIndicator)
        }

        /**
         * Returns an `Indicator` whose value is `(indicator * coefficient)`.
         *
         * @param indicator the indicator
         * @param coefficient the coefficient to multiply by
         *
         * @return `indicator * coefficient`, rounded as necessary
         */
        @JvmStatic
        fun product(indicator: NumericIndicator, coefficient: Number): BinaryOperation {
            val constantIndicator = ConstantNumericIndicator(indicator.numFactory.numOf(coefficient))
            return BinaryOperation({ a, b -> a * b }, indicator, constantIndicator)
        }

        /**
         * Returns an `Indicator` whose value is `(indicator / coefficient)`.
         *
         * @param indicator the indicator
         * @param coefficient the coefficient to divide by
         *
         * @return `indicator / coefficient`, rounded as necessary
         */
        @JvmStatic
        fun quotient(indicator: NumericIndicator, coefficient: Number): BinaryOperation {
            val constantIndicator = ConstantNumericIndicator(indicator.numFactory.numOf(coefficient))
            return BinaryOperation({ a, b -> a / b }, indicator, constantIndicator)
        }

        /**
         * Returns the minimum of `indicator` and `coefficient` as an
         * `Indicator`.
         *
         * @param indicator the indicator
         * @param coefficient the coefficient to compare with
         *
         * @return the `Indicator` whose value is the smaller of `indicator` and
         * `coefficient`. If they are equal, `indicator` is returned.
         */
        @JvmStatic
        fun min(indicator: NumericIndicator, coefficient: Number): BinaryOperation {
            val constantIndicator = ConstantNumericIndicator(indicator.numFactory.numOf(coefficient))
            return BinaryOperation({ a, b -> minOf(a, b) }, indicator, constantIndicator)
        }

        /**
         * Returns the maximum of `indicator` and `coefficient` as an
         * `Indicator`.
         *
         * @param indicator the indicator
         * @param coefficient the coefficient to compare with
         *
         * @return the `Indicator` whose value is the greater of `indicator` and
         * `coefficient`. If they are equal, `indicator` is returned.
         */
        @JvmStatic
        fun max(indicator: NumericIndicator, coefficient: Number): BinaryOperation {
            val constantIndicator = ConstantNumericIndicator(indicator.numFactory.numOf(coefficient))
            return BinaryOperation({ a, b -> maxOf(a, b) }, indicator, constantIndicator)
        }
    }
}
