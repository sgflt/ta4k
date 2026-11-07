/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Bollinger BandWidth indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:bollinger_band_width](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:bollinger_band_width)
 */
class BollingerBandWidthIndicator
/**
 * Constructor.
 *
 * @param bbu the upper band Indicator.
 * @param bbm the middle band Indicator. Typically an `SMAIndicator` is
 * used.
 * @param bbl the lower band Indicator.
 */(
    private val bbu: BollingerBandsUpperIndicator,
    private val bbm: BollingerBandsMiddleIndicator,
    private val bbl: BollingerBandsLowerIndicator,
) : NumericIndicator(bbm.numFactory) {
    private fun calculate() = (bbu.value - bbl.value) / bbm.value * numFactory.hundred()

    override fun updateState(bar: Bar) {
        bbl.onBar(bar)
        bbm.onBar(bar)
        bbu.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = bbl.isStable && bbu.isStable && bbm.isStable

    override val lag: Int
        get() = maxOf(bbl.lag, bbu.lag, bbm.lag)

    override fun toString() = "BolBaWidth(${bbu}, ${bbm}, ${bbl}) => $value"
}
