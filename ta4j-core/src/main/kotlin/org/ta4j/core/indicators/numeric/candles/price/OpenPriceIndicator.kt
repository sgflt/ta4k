/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles.price

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Open price indicator.
 *
 *
 *
 * Returns the open price of a bar.
 */
class OpenPriceIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {
    override fun updateState(bar: Bar) {
        value = bar.openPrice
    }


    override fun toString(): String {
        return String.format("Open() => %s", value)
    }

    override val lag = 0
}
