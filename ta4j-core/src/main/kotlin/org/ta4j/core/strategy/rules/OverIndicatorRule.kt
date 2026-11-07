/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * A rule that monitors when one [indicator][Indicator] is over another.
 *
 *
 *
 * Satisfied when the value of the first [indicator][Indicator] is strictly
 * greater than the value of the second one.
 */
class OverIndicatorRule(
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
        indicator.numFactory.numOf(threshold)
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
            val satisfied = first.isGreaterThan(second)
            traceIsSatisfied(satisfied)
            return satisfied
        }


    override fun toString() = "OverRule[$first, $second] => ${isSatisfied}"
}
