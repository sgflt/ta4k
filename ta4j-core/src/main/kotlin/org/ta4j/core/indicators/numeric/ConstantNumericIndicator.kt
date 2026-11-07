/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric

import org.ta4j.core.api.series.Bar
import org.ta4j.core.num.Num

/**
 * Constant indicator that returns a constant value for a bar.
 */
class ConstantNumericIndicator(t: Num) : NumericIndicator(t.numFactory) {
    init {
        value = t
    }


    override fun updateState(bar: Bar) {
        // NOOP
    }

    override val lag = 0

    override val isStable = true

    override fun toString() = "C => $value"
}
