/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * A rule that monitors when one [indicator][Indicator] is below another.
 *
 *
 *
 * Satisfied when the value of the first [indicator][Indicator] is strictly
 * less than the value of the second one.
 */
class UnderIndicatorRule
/**
 * Constructor.
 *
 * @param first the first indicator
 * @param second the second indicator
 */(
    /** The first indicator.  */
    private val first: NumericIndicator,
    /** The second indicator.  */
    private val second: NumericIndicator,
) : AbstractRule() {
    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param threshold the threshold
     */
    constructor(indicator: NumericIndicator, threshold: Number) : this(
        indicator,
        ConstantNumericIndicator(indicator.numFactory.numOf(threshold))
    )


    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param threshold the threshold
     */
    constructor(indicator: NumericIndicator, threshold: Num) : this(indicator, ConstantNumericIndicator(threshold))


    override val isSatisfied: Boolean
        get() {
            val satisfied = first.isLessThan(second)
            traceIsSatisfied(satisfied)
            return satisfied
        }


    override fun toString() = "UnderRule[$first, $second] => ${isSatisfied}"
}
