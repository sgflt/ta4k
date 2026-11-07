/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators.vortex

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.NumFactory

/**
 * Vortex movement: VM+ = |high - previous low| for [positive], VM- = |low - previous high| otherwise.
 * NaN on the first bar.
 */
class VortexMovementIndicator(numFactory: NumFactory, private val positive: Boolean) : NumericIndicator(numFactory) {
    private var previousBar: Bar? = null

    override fun updateState(bar: Bar) {
        val previous = previousBar
        value = when {
            previous == null -> NaN
            positive -> (bar.highPrice - previous.lowPrice).abs()
            else -> (bar.lowPrice - previous.highPrice).abs()
        }
        previousBar = bar
    }

    override val lag = 2

    override val isStable: Boolean
        get() = previousBar != null && !value.isNaN

    override fun toString() = "VM${if (positive) "+" else "-"} => $value"
}
