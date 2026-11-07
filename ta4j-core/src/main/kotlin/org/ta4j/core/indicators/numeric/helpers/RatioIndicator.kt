/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Calculates the ratio between the current and the previous indicator value.
 *
 * <pre>
 * ratio = currentBarIndicatorValue / previousBarIndicatorValue
 * </pre>
 */
class RatioIndicator(private val indicator: NumericIndicator) : NumericIndicator(indicator.numFactory) {
    private val previousIndicator = indicator.previous()

    /**
     * Calculates the ratio between the current and previous indicator values.
     *
     * @return the ratio between the indicator values
     */
    private fun calculate(): Num {
        if (!previousIndicator.isStable) {
            return indicator.value
        }

        val currentValue = indicator.value
        val previousValue = previousIndicator.value

        return if (previousValue.isZero) {
            numFactory.zero()
        } else {
            currentValue / previousValue
        }
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        previousIndicator.onBar(bar)
        value = calculate()
    }

    override val lag = 1

    override val isStable: Boolean
        get() = indicator.isStable && previousIndicator.isStable

    override fun toString(): String {
        return "RATIO($indicator) => $value"
    }
}
