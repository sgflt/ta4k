/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.SMAIndicator
import org.ta4j.core.indicators.numeric.statistics.StandardDeviationIndicator
import org.ta4j.core.num.Num

/**
 * %B indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:bollinger_band_perce](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:bollinger_band_perce)
 */
class PercentBIndicator(private val indicator: NumericIndicator, barCount: Int, k: Double) :
    NumericIndicator(indicator.numFactory) {
    private val bbu: BollingerBandsUpperIndicator
    private val bbl: BollingerBandsLowerIndicator


    /**
     * Constructor.
     *
     * @param indicator the [Indicator] (usually `ClosePriceIndicator`)
     * @param barCount the time frame
     * @param k the K multiplier (usually 2.0)
     */
    init {
        val bbm = BollingerBandsMiddleIndicator(SMAIndicator(indicator, barCount))
        val sd = StandardDeviationIndicator(indicator, barCount)
        bbu = BollingerBandsUpperIndicator(bbm, sd, numFactory.numOf(k))
        bbl = BollingerBandsLowerIndicator(bbm, sd, numFactory.numOf(k))
    }

    private fun calculate(): Num {
        return (indicator.value - bbl.value) / (bbu.value - bbl.value)
    }

    public override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        bbl.onBar(bar)
        bbu.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = indicator.isStable && bbl.isStable && bbu.isStable

    override fun toString() = "%%Bi(${indicator}) => $value"

    override val lag: Int
        get() = maxOf(indicator.lag, bbl.lag, bbu.lag)
}
