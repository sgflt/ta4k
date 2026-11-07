/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles.price

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Low price indicator.
 *
 * Returns the low price of a bar.
 */
class LowPriceIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {
    override fun updateState(bar: Bar) {
        value = bar.lowPrice
    }


    override fun toString() = "Low() => $value"

    override val lag: Int = 0
}
