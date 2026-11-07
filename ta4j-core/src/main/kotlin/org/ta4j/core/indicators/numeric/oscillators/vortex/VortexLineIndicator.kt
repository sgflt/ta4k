/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators.vortex

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.TRIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.NumFactory

/**
 * Vortex line: +VI = sum(VM+, N) / sum(TR, N) for [positive], -VI with VM- otherwise.
 */
class VortexLineIndicator(
    numFactory: NumFactory,
    private val positive: Boolean,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    init {
        require(barCount > 1) { "Vortex bar count must be greater than 1, but was: $barCount" }
    }

    private val movementSum = VortexMovementIndicator(numFactory, positive).runningTotal(barCount)
    private val trueRangeSum = TRIndicator(numFactory).runningTotal(barCount)

    override fun updateState(bar: Bar) {
        movementSum.onBar(bar)
        trueRangeSum.onBar(bar)
        val range = trueRangeSum.value
        value = if (!movementSum.isStable || range.isNaN || range.isZero) NaN else movementSum.value / range
    }

    override val lag = barCount + 1

    override val isStable: Boolean
        get() = movementSum.isStable && trueRangeSum.isStable

    override fun toString() = "${if (positive) "+" else "-"}VI($barCount) => $value"
}
