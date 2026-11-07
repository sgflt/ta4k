/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.MMAIndicator
import org.ta4j.core.indicators.numeric.candles.TRIndicator
import org.ta4j.core.num.NumFactory

/**
 * Average true range indicator.
 */
class ATRIndicator(
    numFactory: NumFactory,
    tr: NumericIndicator = TRIndicator(numFactory),
    barCount: Int,
) :
    NumericIndicator(numFactory) {
    private val averageTrueRangeIndicator = MMAIndicator(tr, barCount)

    init {
        require(barCount > 0) { "barCount must be positive" }
    }

    override val lag: Int
        get() = averageTrueRangeIndicator.lag

    override fun updateState(bar: Bar) {
        averageTrueRangeIndicator.onBar(bar)
        value = calculate()
    }

    private fun calculate() = averageTrueRangeIndicator.value

    override val isStable: Boolean
        get() = averageTrueRangeIndicator.isStable

    override fun toString() = "ATR($averageTrueRangeIndicator) => $value"
}
