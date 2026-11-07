/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Boolean indicator that returns true when the value of the [indicator][NumericIndicator]
 * is the highest within the `barCount`.
 */
class IsHighestIndicator(
    /** The actual indicator. */
    private val ref: NumericIndicator,
    /** The barCount. */
    private val barCount: Int,
) : BooleanIndicator() {

    /** Pre-computed highest value indicator for efficiency */
    private val highest = ref.highest(barCount)

    override fun updateState(bar: Bar) {
        ref.onBar(bar)
        highest.onBar(bar)

        val refVal = ref.value
        val highestVal = highest.value

        value = !refVal.isNaN && !highestVal.isNaN && refVal == highestVal
    }

    override val lag = highest.lag

    override val isStable: Boolean
        get() = ref.isStable && highest.isStable

    override fun toString(): String {
        return "IsHighest($ref, $barCount) => $value"
    }
}
