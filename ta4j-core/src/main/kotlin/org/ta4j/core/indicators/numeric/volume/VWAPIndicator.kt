/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * The volume-weighted average price (VWAP) Indicator.
 *
 * @param numFactory the number factory
 * @param barCount the time frame (lookback period)
 *
 * @see <a href=
 *      "http://www.investopedia.com/articles/trading/11/trading-with-vwap-mvwap.asp">
 *      http://www.investopedia.com/articles/trading/11/trading-with-vwap-mvwap.asp</a>
 * @see <a href=
 *      "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:vwap_intraday">
 *      http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:vwap_intraday</a>
 * @see <a href="https://en.wikipedia.org/wiki/Volume-weighted_average_price">
 *      https://en.wikipedia.org/wiki/Volume-weighted_average_price</a>
 */
class VWAPIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val typicalPrice = Indicators.extended(numFactory).typicalPrice()
    private val volume = Indicators.extended(numFactory).volume()

    private val typicalPriceVolume = typicalPrice.multipliedBy(volume)
    private val cumulativeTPV = typicalPriceVolume.runningTotal(barCount)
    private val cumulativeVolume = volume.runningTotal(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate() = if (cumulativeVolume.value.isZero) {
        typicalPrice.value
    } else {
        cumulativeTPV.value / cumulativeVolume.value
    }

    override fun updateState(bar: Bar) {
        cumulativeTPV.onBar(bar)
        cumulativeVolume.onBar(bar)

        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = cumulativeTPV.isStable && cumulativeVolume.isStable

    override fun toString(): String {
        return "VWAP($barCount) => $value"
    }
}
