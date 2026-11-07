/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Indicator to calculate the average of a list of other indicators.
 *
 * @param numFactory the factory for creating Num instances
 * @param indicators the indicators to calculate the average from
 * @constructor creates an AverageIndicator with the given indicators
 */
class AverageIndicator(
    numFactory: NumFactory,
    private vararg val indicators: NumericIndicator
) : NumericIndicator(numFactory) {
    
    init {
        require(indicators.isNotEmpty()) { "At least one indicator must be provided" }
    }
    
    private fun calculate(): Num {
        val sum = indicators.fold(numFactory.zero()) { acc, indicator ->
            acc + indicator.value
        }
        return sum / numFactory.numOf(indicators.size)
    }
    
    override fun updateState(bar: Bar) {
        indicators.forEach { it.onBar(bar) }
        value = calculate()
    }
    
    override val lag: Int
        get() = indicators.maxOf { it.lag }
    
    override val isStable: Boolean
        get() = indicators.all { it.isStable }
    
    override fun toString(): String {
        return "AVG(${indicators.joinToString(", ")}) => $value"
    }
}
