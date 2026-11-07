/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles.price

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Average high-low indicator.
 *
 *
 *
 * Returns the median price of a bar using the following formula:
 *
 * <pre>
 * MedianPrice = (highPrice + lowPrice) / 2
</pre> *
 */
class MedianPriceIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {
    private fun calculate(bar: Bar) = (bar.highPrice + bar.lowPrice) / numFactory.two()

    public override fun updateState(bar: Bar) {
        value = calculate(bar)
    }

    override val lag: Int = 0
}
