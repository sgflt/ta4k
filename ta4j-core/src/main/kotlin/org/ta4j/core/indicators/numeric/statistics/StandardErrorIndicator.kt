/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Standard error indicator.
 */
class StandardErrorIndicator(indicator: NumericIndicator, barCount: Int) : NumericIndicator(indicator.numFactory) {
    private val divisor = numFactory.numOf(barCount).sqrt()
    private val sdev = indicator.stddev(barCount)

    override val lag: Int
        get() = sdev.lag

    private fun calculate() = sdev.value / divisor


    override fun updateState(bar: Bar) {
        sdev.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = sdev.isStable
}
