/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.VolumeIndicator
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * On-Balance Volume (OBV) indicator.
 *
 * On-Balance Volume is a momentum indicator that uses volume flow to predict changes in stock price.
 * It relates price and volume by keeping a running total of volume based on price direction:
 *
 * - If today's close > yesterday's close: add today's volume to OBV
 * - If today's close < yesterday's close: subtract today's volume from OBV
 * - If today's close = yesterday's close: OBV remains unchanged
 *
 * @param numFactory the number factory
 *
 * @see <a href="https://www.investopedia.com/terms/o/onbalancevolume.asp">
 *      On-Balance Volume (OBV)</a>
 * @see <a href="https://school.stockcharts.com/doku.php?id=technical_indicators:on_balance_volume_obv">
 *      On Balance Volume - StockCharts</a>
 */
class OnBalanceVolumeIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {

    private val closePrice = ClosePriceIndicator(numFactory)
    private val volume = VolumeIndicator(numFactory)

    private var previousClose = closePrice.previous()
    private var obv = numFactory.zero()

    private fun calculate(): Num {
        val currentClose = closePrice.value
        val currentVolume = volume.value

        previousClose.value.let { prevClose ->
            when {
                currentClose > prevClose -> obv += currentVolume
                currentClose < prevClose -> obv -= currentVolume
            }
        }

        return obv
    }

    override fun updateState(bar: Bar) {
        previousClose.onBar(bar)
        volume.onBar(bar)
        value = calculate()
    }

    override val lag = 0

    override val isStable = true

    override fun toString(): String {
        return "OBV => $value"
    }
}
