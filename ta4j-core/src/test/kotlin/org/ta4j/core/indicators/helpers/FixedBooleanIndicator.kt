/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator

/**
 * A fixed boolean indicator.
 *
 *
 *
 * Returns constant [Boolean] values for a bar.
 */
class FixedBooleanIndicator(vararg values: Boolean?) : BooleanIndicator() {
    private val indicator: FixedIndicator<Boolean?> = FixedIndicator<Boolean?>(*values)


    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = indicator.value!!
    }

    override val lag
        get() = indicator.lag

    override val isStable
        get() = indicator.isStable
}
