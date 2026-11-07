/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * The volume-weighted average price (VWAP) over the last [barCount] bars.
 *
 * @param price the price to weight, typical price by default
 * @param volume the weights, bar volume by default
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
    private val barCount: Int,
    price: NumericIndicator,
    volume: NumericIndicator,
) : AbstractVWAPIndicator(price, volume) {

    constructor(numFactory: NumFactory, barCount: Int) : this(
        barCount,
        Indicators.extended(numFactory).typicalPrice(),
        Indicators.extended(numFactory).volume(),
    )

    private val window = ArrayDeque<Pair<Num, Num>>(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    override fun updateWindow(bar: Bar, price: Num, volume: Num) {
        if (window.size == barCount) {
            val (oldPrice, oldVolume) = window.removeFirst()
            remove(oldPrice, oldVolume)
        }
        window.addLast(price to volume)
        add(price, volume)
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = window.size >= barCount && price.isStable && volume.isStable

    override fun toString() = "VWAP($barCount) => $value"
}
