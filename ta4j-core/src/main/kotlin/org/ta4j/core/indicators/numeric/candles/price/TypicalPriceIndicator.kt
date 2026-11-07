/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles.price

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Typical price indicator.
 *
 *
 *
 * Returns the typical price of a bar using the following formula:
 *
 * <pre>
 * TypicalPrice = (highPrice + lowPrice + closePrice) / 3
</pre> *
 */
class TypicalPriceIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {
    private fun calculate(bar: Bar): Num = with(bar) {
        (highPrice + lowPrice + closePrice) / numFactory.three()
    }


    override fun updateState(bar: Bar) {
        value = calculate(bar)
    }

    override val lag = 0
}
