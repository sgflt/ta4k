/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.ichimoku

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Ichimoku clouds: Senkou Span A (Leading Span A) indicator.
 *
 * @see [Ichimoku Cloud](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud)
 */
class IchimokuSenkouSpanAIndicator(
    numFactory: NumFactory,
    tenkanSenBarCount: Int = 9,
    kijunSenBarCount: Int = 26,
    private val conversionLine: IchimokuTenkanSenIndicator = IchimokuTenkanSenIndicator(numFactory, tenkanSenBarCount),
    private val baseLine: IchimokuKijunSenIndicator = IchimokuKijunSenIndicator(numFactory, kijunSenBarCount),
    offset: Int = 26,
) : NumericIndicator(numFactory) {

    override val lag: Int = offset + maxOf(conversionLine.lag, baseLine.lag)

    override val isStable: Boolean
        get() = conversionLine.isStable && baseLine.isStable

    private fun calculate(): Num = if (conversionLine.value == NaN || baseLine.value == NaN) {
        NaN
    } else {
        (conversionLine.value + baseLine.value) / numFactory.two()
    }

    override fun updateState(bar: Bar) {
        // Update the underlying indicators
        conversionLine.onBar(bar)
        baseLine.onBar(bar)

        // Calculate the average of conversion line and base line
        // Note: This indicator is forward-shifted by 'offset' periods
        value = calculate()
    }
}
