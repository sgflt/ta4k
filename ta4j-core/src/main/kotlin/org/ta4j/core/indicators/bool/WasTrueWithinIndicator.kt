/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool

import org.ta4j.core.api.series.Bar

/**
 * True when [indicator] was true on at least one of the last [barCount] bars, current bar included.
 *
 * Keeps only the distance to the last true bar, so it needs no window buffer.
 */
class WasTrueWithinIndicator(
    private val indicator: BooleanIndicator,
    private val barCount: Int,
) : BooleanIndicator() {

    private var barsSinceTrue = Int.MAX_VALUE
    private var barsPassed = 0

    init {
        require(barCount > 0) { "Bar count must be positive, but was: $barCount" }
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        ++barsPassed
        barsSinceTrue = if (indicator.value) 0 else barsSinceTrue.saturatingIncrement()
        value = barsSinceTrue < barCount
    }

    private fun Int.saturatingIncrement() = if (this == Int.MAX_VALUE) this else this + 1

    override val lag = barCount

    override val isStable: Boolean
        get() = barsPassed >= barCount && indicator.isStable

    override fun toString() = "WasTrueWithin($barCount) => $value"
}
