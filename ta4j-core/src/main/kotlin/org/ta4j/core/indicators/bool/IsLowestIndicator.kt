/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Boolean indicator that returns true when the value of the [indicator][NumericIndicator]
 * is the lowest within the `barCount`.
 */
class IsLowestIndicator(
    /** The actual indicator. */
    private val ref: NumericIndicator,
    /** The barCount. */
    private val barCount: Int,
) : BooleanIndicator() {

    /** Pre-computed lowest value indicator for efficiency */
    private val lowest = ref.lowest(barCount)

    override fun updateState(bar: Bar) {
        ref.onBar(bar)
        lowest.onBar(bar)

        val refVal = ref.value
        val lowestVal = lowest.value

        value = !refVal.isNaN && !lowestVal.isNaN && refVal == lowestVal
    }

    override val lag = lowest.lag

    override val isStable: Boolean
        get() = ref.isStable && lowest.isStable

    override fun toString(): String {
        return "IsLowest($ref, $barCount) => $value"
    }
}
