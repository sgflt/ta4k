/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Base class for rules comparing the current values of two indicators.
 *
 * @param first the indicator on the left side of the comparison
 * @param second the indicator on the right side of the comparison
 * @param label short name used in [toString]
 */
abstract class AbstractIndicatorComparisonRule(
    protected val first: NumericIndicator,
    protected val second: NumericIndicator,
    private val label: String,
) : AbstractRule() {

    /**
     * @return true if [firstValue] relates to [secondValue] as this rule requires
     */
    protected abstract fun compare(firstValue: Num, secondValue: Num): Boolean

    final override val isSatisfied: Boolean
        get() {
            val satisfied = compare(first.value, second.value)
            traceIsSatisfied(satisfied)
            return satisfied
        }

    override fun toString() = "$label[$first, $second] => $isSatisfied"
}
