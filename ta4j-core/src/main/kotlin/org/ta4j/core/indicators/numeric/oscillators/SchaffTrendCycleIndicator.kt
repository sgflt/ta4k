/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Schaff Trend Cycle: MACD put through two rounds of stochastic + EMA smoothing, giving a 0-100
 * trend oscillator that turns faster than MACD.
 *
 * @see <a href="https://www.investopedia.com/articles/forex/10/schaff-trend-cycle-indicator.asp">Investopedia</a>
 */
class SchaffTrendCycleIndicator(
    indicator: NumericIndicator,
    private val fastPeriod: Int = 23,
    private val slowPeriod: Int = 50,
    private val cycleLength: Int = 10,
    private val smoothingPeriod: Int = 3,
) : NumericIndicator(indicator.numFactory) {

    init {
        require(fastPeriod > 0 && slowPeriod > 0 && cycleLength > 0 && smoothingPeriod > 0) {
            "All Schaff Trend Cycle periods must be positive"
        }
        require(slowPeriod > fastPeriod) { "Slow period must be greater than fast period" }
    }

    private val cycle = StochasticIndicator(
        StochasticIndicator(indicator.macd(fastPeriod, slowPeriod), cycleLength).ema(smoothingPeriod),
        cycleLength
    ).ema(smoothingPeriod)

    override fun updateState(bar: Bar) {
        cycle.onBar(bar)
        value = cycle.value
    }

    override val lag = slowPeriod + 2 * (cycleLength + smoothingPeriod)

    override val isStable: Boolean
        get() = cycle.isStable

    override fun toString() = "STC($fastPeriod, $slowPeriod, $cycleLength, $smoothingPeriod) => $value"
}
