/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Satisfied when the value of the first [NumericIndicator] is equal to
 * the value of the second one.
 */
class IsEqualRule(
    /** The first indicator. */
    private val first: NumericIndicator,
    /** The second indicator. */
    private val second: NumericIndicator,
) : AbstractRule() {

    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param value the value to check
     */
    constructor(indicator: NumericIndicator, value: Number) : this(
        indicator,
        ConstantNumericIndicator(indicator.numFactory.numOf(value))
    )

    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param value the value to check
     */
    constructor(indicator: NumericIndicator, value: Num) : this(
        indicator,
        ConstantNumericIndicator(value)
    )

    override val isSatisfied: Boolean
        get() {
            val satisfied = first.value == second.value
            traceIsSatisfied(satisfied)
            return satisfied
        }

    override fun toString(): String {
        return "IsEqualRule[$first, $second] => $isSatisfied"
    }
}
