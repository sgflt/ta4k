/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.statistics.PercentRankIndicator
import org.ta4j.core.num.NaN

/**
 * Connors RSI: average of RSI(price, [rsiPeriod]), RSI(streak, [streakRsiPeriod]) and the percent rank
 * of the one-bar price change over [percentRankPeriod] bars.
 *
 * @see <a href="https://chartschool.stockcharts.com/table-of-contents/technical-indicators-and-overlays/technical-indicators/connorsrsi">StockCharts</a>
 */
class ConnorsRSIIndicator(
    indicator: NumericIndicator,
    private val rsiPeriod: Int = 3,
    private val streakRsiPeriod: Int = 2,
    private val percentRankPeriod: Int = 100,
) : NumericIndicator(indicator.numFactory) {

    init {
        require(rsiPeriod > 0 && streakRsiPeriod > 0 && percentRankPeriod > 0) {
            "Connors RSI periods must be positive"
        }
    }

    private val priceRsi = indicator.rsi(rsiPeriod)
    private val streakRsi = indicator.streak().rsi(streakRsiPeriod)
    private val changeRank = PercentRankIndicator(indicator.difference(), percentRankPeriod)
    private val three = numFactory.numOf(3)

    override fun updateState(bar: Bar) {
        priceRsi.onBar(bar)
        streakRsi.onBar(bar)
        changeRank.onBar(bar)
        val parts = listOf(priceRsi.value, streakRsi.value, changeRank.value)
        value = if (parts.any { it.isNaN }) NaN else (parts[0] + parts[1] + parts[2]) / three
    }

    override val lag = maxOf(rsiPeriod, streakRsiPeriod, percentRankPeriod + 1)

    override val isStable: Boolean
        get() = priceRsi.isStable && streakRsi.isStable && changeRank.isStable

    override fun toString() = "ConnorsRSI($rsiPeriod, $streakRsiPeriod, $percentRankPeriod) => $value"
}
