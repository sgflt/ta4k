/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.num.NumFactory

/**
 * Doji indicator.
 *
 *
 *
 * A candle/bar is considered Doji if its body height is lower than the average
 * multiplied by a factor.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks.doji](http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks.doji)
 */
class DojiIndicator(numFactory: NumFactory, barCount: Int, bodyFactor: Double) : BooleanIndicator() {
    /** Body height.  */
    private val bodyHeightInd = Indicators.extended(numFactory).realBody().abs()

    /** Average body height.  */
    private val averageBodyHeightInd = bodyHeightInd.sma(barCount).previous()

    /** The factor used when checking if a candle is Doji.  */
    private val factor = numFactory.numOf(bodyFactor)

    override val lag: Int
        get() = averageBodyHeightInd.lag


    private fun calculate(): Boolean {
        val averageBodyHeight = averageBodyHeightInd.value
        val currentBodyHeight = bodyHeightInd.value

        return currentBodyHeight < averageBodyHeight * factor
    }


    public override fun updateState(bar: Bar) {
        bodyHeightInd.onBar(bar)
        averageBodyHeightInd.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = averageBodyHeightInd.isStable
}
