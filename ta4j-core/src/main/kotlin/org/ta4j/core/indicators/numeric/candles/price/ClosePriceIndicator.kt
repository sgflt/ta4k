/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles.price

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Close price indicator.
 *
 *
 *
 * Returns the close price of a bar.
 */
class ClosePriceIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {
    override fun updateState(bar: Bar) {
        value = bar.closePrice
    }

    override fun toString() = "Close() => $value"

    override val lag: Int = 0
}
