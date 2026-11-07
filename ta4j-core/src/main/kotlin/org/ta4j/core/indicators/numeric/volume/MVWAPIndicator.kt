/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * The Moving volume weighted average price (MVWAP) Indicator.
 *
 * MVWAP is a simple moving average applied to VWAP values over a specified period.
 * This creates a smoother version of VWAP that reduces noise while maintaining
 * the volume-weighted characteristics.
 *
 * @param numFactory the number factory
 * @param vwapBarCount the time frame for VWAP calculation
 * @param mvwapBarCount the time frame for the moving average of VWAP
 *
 * @see <a href=
 *      "http://www.investopedia.com/articles/trading/11/trading-with-vwap-mvwap.asp">
 *      http://www.investopedia.com/articles/trading/11/trading-with-vwap-mvwap.asp</a>
 */
class MVWAPIndicator(
    numFactory: NumFactory,
    vwapBarCount: Int,
    private val mvwapBarCount: Int,
) : NumericIndicator(numFactory) {

    private val sma = VWAPIndicator(numFactory, vwapBarCount).sma(mvwapBarCount)

    init {
        require(mvwapBarCount > 0) { "MVWAP bar count must be positive" }
    }

    private fun calculate() = sma.value

    override fun updateState(bar: Bar) {
        sma.onBar(bar)
        value = calculate()
    }

    override val lag = sma.lag

    override val isStable: Boolean
        get() = sma.isStable

    override fun toString(): String {
        return "MVWAP($mvwapBarCount) => $value"
    }
}
