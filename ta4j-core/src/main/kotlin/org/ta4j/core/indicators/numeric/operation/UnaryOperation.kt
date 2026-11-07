/*
 * SPDX-License-Identifier: MIT
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
