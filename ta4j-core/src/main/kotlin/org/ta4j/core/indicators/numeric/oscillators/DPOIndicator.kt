/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * The Detrended Price Oscillator (DPO) indicator.
 *
 * The Detrended Price Oscillator (DPO) is an indicator designed to remove trend
 * from price and make it easier to identify cycles. DPO does not extend to the
 * last date because it is based on a displaced moving average. However,
 * alignment with the most recent is not an issue because DPO is not a momentum
 * oscillator. Instead, DPO is used to identify cycles highs/lows and estimate
 * cycle length.
 *
 * In short, DPO(20) equals price 11 days ago less the 20-day SMA.
 *
 * @param numFactory the number factory
 * @param indicator the price indicator
 * @param barCount the time frame
 *
 * @see <a href=
 *      "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:detrended_price_osci">
 *      http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:detrended_price_osci</a>
 */
class DPOIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val smaIndicator = indicator.sma(barCount)
    private val timeFrames = barCount / 2 + 1
    private val previousSmaIndicator = smaIndicator.previous(timeFrames)
    private var barsProcessed = 0

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate() = indicator.value - previousSmaIndicator.value

    override fun updateState(bar: Bar) {
        ++barsProcessed
        smaIndicator.onBar(bar)
        previousSmaIndicator.onBar(bar)

        value = calculate()
    }

    override val lag = barCount + timeFrames

    override val isStable: Boolean
        get() = barsProcessed >= lag && smaIndicator.isStable && previousSmaIndicator.isStable

    override fun toString(): String = "DPO($barCount) => $value"
}
