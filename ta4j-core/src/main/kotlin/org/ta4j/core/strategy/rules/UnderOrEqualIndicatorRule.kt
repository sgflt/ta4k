/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Satisfied when the value of the first indicator is less than or equal to the value of the second one.
 *
 * @param first the first indicator
 * @param second the second indicator
 */
class UnderOrEqualIndicatorRule(
    first: NumericIndicator,
    second: NumericIndicator,
) : AbstractIndicatorComparisonRule(first, second, "UnderOrEqualRule") {

    /**
     * @param indicator the indicator
     * @param threshold the threshold
     */
    constructor(indicator: NumericIndicator, threshold: Number) : this(indicator, indicator.numFactory.numOf(threshold))

    /**
     * @param indicator the indicator
     * @param threshold the threshold
     */
    constructor(indicator: NumericIndicator, threshold: Num) : this(indicator, ConstantNumericIndicator(threshold))

    override fun compare(firstValue: Num, secondValue: Num) = firstValue <= secondValue
}
