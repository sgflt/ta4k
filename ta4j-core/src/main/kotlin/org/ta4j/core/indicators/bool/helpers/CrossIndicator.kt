/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Cross indicator.
 *
 * Boolean indicator that monitors the crossing of two indicators.
 */
class CrossIndicator(
    val up: NumericIndicator,
    val low: NumericIndicator,
    barCount: Int,
) : BooleanIndicator() {

    private val previousUp: PreviousNumericValueIndicator = up.previous(barCount)
    private val previousLow: PreviousNumericValueIndicator = low.previous(barCount)

    private fun calculate() = up.value > low.value && previousUp.value <= previousLow.value

    override fun updateState(bar: Bar) {
        low.onBar(bar)
        up.onBar(bar)
        previousUp.onBar(bar)
        previousLow.onBar(bar)
        value = calculate()
    }

    override val lag
        get() = previousUp.lag

    override val isStable
        get() = up.isStable && low.isStable && previousUp.isStable && previousLow.isStable

    override fun toString() =
        "Cross($up, $low, $previousUp, $previousLow) => $value"
}

