/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.fractal

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Price of the most recently confirmed fractal pivot (recent swing high or low), NaN until the first one.
 */
class RecentFractalSwingIndicator(
    numFactory: NumFactory,
    private val fractal: AbstractFractalIndicator,
) : NumericIndicator(numFactory) {

    override fun updateState(bar: Bar) {
        fractal.onBar(bar)
        if (fractal.value) value = fractal.pivotPrice
    }

    override val lag: Int
        get() = fractal.lag

    override val isStable: Boolean
        get() = !value.isNaN

    override fun toString() = "RecentSwing($fractal) => $value"
}
