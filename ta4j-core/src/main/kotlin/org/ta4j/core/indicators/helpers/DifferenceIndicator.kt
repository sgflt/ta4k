/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Calculates the difference between the current and the previous indicator value.
 *
 * <pre>
 * difference = current indicator value - previous indicator value
</pre> *
 */
class DifferenceIndicator(private val indicator: NumericIndicator) : NumericIndicator(indicator.numFactory) {
    private var previousValue = indicator.previous()


    public override fun updateState(bar: Bar) {
        previousValue.onBar(bar)
        value = calculate()
    }

    /**
     * Calculates the difference between indicator values of the current bar and the
     * previous bar.
     *
     * @return the difference between the close prices
     */
    private fun calculate() = indicator.value - previousValue.value


    override val lag = 2


    override val isStable
        get() = previousValue.isStable
}
