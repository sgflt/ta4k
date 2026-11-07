/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Moving average convergence divergence (MACD) indicator (also called
 * "MACD Absolute Price Oscillator (APO)").
 *
 * @param numFactory the number factory
 * @param indicator the source indicator
 * @param shortBarCount the short time frame (normally 12)
 * @param longBarCount the long time frame (normally 26)
 *
 * @see <a href=
 *      "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:moving_average_convergence_divergence_macd">
 *      http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:moving_average_convergence_divergence_macd</a>
 */
class MACDIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val shortBarCount: Int = 12,
    private val longBarCount: Int = 26,
) : NumericIndicator(numFactory) {

    val shortTermEma = indicator.ema(shortBarCount)
    val longTermEma = indicator.ema(longBarCount)

    init {
        require(shortBarCount < longBarCount) {
            "Long term period count must be greater than short term period count"
        }
    }

    private fun calculate() = shortTermEma.value - longTermEma.value

    override fun updateState(bar: Bar) {
        shortTermEma.onBar(bar)
        longTermEma.onBar(bar)
        value = calculate()
    }

    override val lag = longBarCount

    override val isStable: Boolean
        get() = shortTermEma.isStable && longTermEma.isStable

    override fun toString(): String {
        return "MACD($shortBarCount, $longBarCount) => $value"
    }
}
