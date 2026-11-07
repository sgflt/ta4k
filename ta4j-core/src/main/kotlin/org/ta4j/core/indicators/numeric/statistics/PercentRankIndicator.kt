/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Percent rank: share of the previous [period] values (current excluded) that are below the
 * current value, from 0 to 100. NaN values in the window are ignored.
 */
class PercentRankIndicator(
    private val indicator: NumericIndicator,
    private val period: Int,
) : NumericIndicator(indicator.numFactory) {

    init {
        require(period > 0) { "Period must be positive, but was: $period" }
    }

    private val window = ArrayDeque<Num>(period)
    private var barsPassed = 0
    private val hundred = numFactory.hundred()

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        ++barsPassed
        val current = indicator.value
        value = rank(current)
        window.addLast(current)
        if (window.size > period) {
            window.removeFirst()
        }
    }

    private fun rank(current: Num): Num {
        if (current.isNaN) {
            return NaN
        }
        var valid = 0
        var below = 0
        for (candidate in window) {
            if (candidate.isNaN) continue
            valid++
            if (candidate < current) below++
        }
        return if (valid == 0) NaN else numFactory.numOf(below) / numFactory.numOf(valid) * hundred
    }

    override val lag = period + 1

    override val isStable: Boolean
        // the rank of this bar was taken over a full window
        get() = barsPassed > period && indicator.isStable

    override fun toString() = "PercentRank($period) => $value"
}
