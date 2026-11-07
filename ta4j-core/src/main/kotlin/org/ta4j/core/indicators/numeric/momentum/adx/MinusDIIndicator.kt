/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum.adx

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * -DI indicator.
 *
 *
 *
 * Part of the Directional Movement System.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx)
 *
 * @see [https://www.investopedia.com/terms/a/adx.asp](https://www.investopedia.com/terms/a/adx.asp)
 */
class MinusDIIndicator(numFactory: NumFactory, private val barCount: Int) : NumericIndicator(numFactory) {
    private val atrIndicator = Indicators.extended(numFactory).atr(barCount)
    private val avgMinusDMIndicator = Indicators.extended(numFactory).minusDMI().mma(barCount)


    private fun calculate() = (avgMinusDMIndicator.value / atrIndicator.value) * numFactory.hundred()


    override fun updateState(bar: Bar) {
        atrIndicator.onBar(bar)
        avgMinusDMIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = atrIndicator.isStable && avgMinusDMIndicator.isStable

    override val lag = barCount

    override fun toString() = "MDII($barCount) => $value"
}
