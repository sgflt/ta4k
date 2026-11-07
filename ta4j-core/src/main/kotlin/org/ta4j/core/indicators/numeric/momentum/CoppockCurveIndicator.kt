/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.WMAIndicator
import org.ta4j.core.indicators.numeric.operation.BinaryOperation
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Coppock Curve indicator.
 *
 * The Coppock Curve is a long-term price momentum oscillator used primarily to identify major bottoms in the stock market.
 * It is calculated as a weighted moving average of the sum of the long-term rate of change and the short-term rate of change.
 *
 * @see <a href=
 *      "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:coppock_curve">
 *      Coppock indicator</a>
 */
class CoppockCurveIndicator(
    numFactory: NumFactory,
    indicator: NumericIndicator,
    longRoCBarCount: Int = 14,
    shortRoCBarCount: Int = 11,
    wmaBarCount: Int = 10,
) : NumericIndicator(numFactory) {
    private val wma = WMAIndicator(
        BinaryOperation.sum(
            ROCIndicator(numFactory, indicator, longRoCBarCount),
            ROCIndicator(numFactory, indicator, shortRoCBarCount)
        ),
        wmaBarCount
    )

    override val lag: Int = wma.lag

    override val isStable: Boolean
        get() = wma.isStable

    private fun calculate(): Num = wma.value

    override fun updateState(bar: Bar) {
        wma.onBar(bar)
        value = calculate()
    }
}
