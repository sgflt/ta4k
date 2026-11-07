/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles.price

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * The high price indicator.
 *
 *
 *
 * Returns the high price of a bar.
 */
class HighPriceIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {
    override fun updateState(bar: Bar) {
        value = bar.highPrice
    }

    override fun toString() = "High() => $value"

    override val lag: Int = 0
}
