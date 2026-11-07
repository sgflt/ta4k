/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import kotlin.math.ceil
import kotlin.math.floor
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Asymmetric Triangular Moving Average (ATMA) Indicator.
 *
 * ATMA is a double-smoothing of the Simple Moving Average (SMA), however unlike
 * the Triangular Moving Average (TMA) the modified equation creates a smoother
 * and slightly lagged moving average compared to the traditional TMA.
 */
class ATMAIndicator(
    indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(indicator.numFactory) {

    private val fast = ceil(barCount / 2.0).toInt()
    private val slow = (floor(barCount / 2.0) + 1).toInt()
    private val sma = indicator.sma(fast)
    private val smaSma = sma.sma(slow)
    private var barsPassed = 0

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = barsPassed >= fast + slow

    override fun updateState(bar: Bar) {
        sma.onBar(bar)
        smaSma.onBar(bar)

        value = smaSma.value
        ++barsPassed
    }

    override fun toString(): String = "ATMA($barCount) => $value"
}
