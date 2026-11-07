/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.price.TypicalPriceIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Commodity Channel Index (CCI) indicator.
 *
 * The CCI measures the variation of a security's price from its statistical mean.
 * High values above +100 may indicate that the security is overbought and due for a correction.
 * Low values below -100 may indicate that the security is oversold and due for a rally.
 *
 * Formula: CCI = (Typical Price - SMA(Typical Price)) / (0.015 * Mean Deviation)
 * Where Typical Price = (High + Low + Close) / 3
 *
 * @param numFactory the number factory
 * @param barCount the time frame (normally 20)
 *
 * @see <a href=
 *      "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:commodity_channel_in">
 *      http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:commodity_channel_in</a>
 */
class CCIIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val factor = numFactory.numOf(0.015)
    private val typicalPrice = TypicalPriceIndicator(numFactory)
    private val sma = typicalPrice.sma(barCount)
    private val meanDeviation = typicalPrice.meanDeviation(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate(): Num {
        val meanDeviationValue = meanDeviation.value
        if (meanDeviationValue.isZero) {
            return numFactory.zero()
        }
        return (typicalPrice.value - sma.value) / (meanDeviationValue * factor)
    }

    override fun updateState(bar: Bar) {
        typicalPrice.onBar(bar)
        sma.onBar(bar)
        meanDeviation.onBar(bar)
        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = sma.isStable && meanDeviation.isStable

    override fun toString(): String {
        return "CCI($barCount) => $value"
    }
}
