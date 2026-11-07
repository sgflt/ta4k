/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Calculates the sum of all indicator values.
 *
 * Sum = summand0 + summand1 + ... + summandN
 */
class SumIndicator(
    numFactory: NumFactory,
    private vararg val summands: NumericIndicator,
) : NumericIndicator(numFactory) {

    init {
        require(summands.isNotEmpty()) { "At least one summand is required" }
    }

    private fun calculate(): Num = summands.fold(numFactory.zero()) { sum, summand ->
        sum + summand.value
    }

    override fun updateState(bar: Bar) {
        summands.forEach { it.onBar(bar) }
        value = calculate()
    }

    override val lag: Int
        get() = summands.maxOfOrNull { it.lag } ?: 0

    override val isStable: Boolean
        get() = summands.all { it.isStable }

    override fun toString(): String {
        return "SUM(${summands.joinToString(", ")}) => $value"
    }
}
