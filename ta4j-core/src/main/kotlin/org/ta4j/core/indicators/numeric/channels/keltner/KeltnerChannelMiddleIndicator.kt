/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.keltner

import org.ta4j.core.api.Indicators.typicalPrice
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Keltner Channel (middle line) indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:keltner_channels](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:keltner_channels)
 */
class KeltnerChannelMiddleIndicator(
    numFactory: NumFactory,
    indicator: NumericIndicator = typicalPrice(),
    barCountEMA: Int,
) :
    NumericIndicator(numFactory) {
    private val emaIndicator = indicator.ema(barCountEMA)

    private fun calculate() = emaIndicator.value

    public override fun updateState(bar: Bar) {
        emaIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = emaIndicator.isStable

    override val lag: Int
        get() = emaIndicator.lag
}
