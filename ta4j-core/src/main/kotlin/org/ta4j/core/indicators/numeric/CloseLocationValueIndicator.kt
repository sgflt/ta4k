/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric

import org.ta4j.core.api.series.Bar
import org.ta4j.core.num.NumFactory

/**
 * Close Location Value (CLV) indicator.
 *
 * @see [Close Location Value](https://www.investopedia.com/terms/c/close_location_value.asp)
 */
class CloseLocationValueIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {
    private fun calculate(bar: Bar) = with(bar) {
        val diffHighLow = highPrice.minus(lowPrice)

        if (diffHighLow.isNaN || diffHighLow.isZero) {
            numFactory.zero()
        } else {
            (closePrice - lowPrice - (highPrice - closePrice)) / diffHighLow
        }
    }

    override fun updateState(bar: Bar) {
        value = calculate(bar)
    }

    override val lag = 0

    override val isStable
        get() = !value.isNaN
}
