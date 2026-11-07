/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Buy - Occurs when the price line crosses from below to above the Lower
 * Bollinger Band.
 *
 *
 *
 * Sell - Occurs when the price line crosses from above to below the Upper
 * Bollinger Band.
 */
class BollingerBandsUpperIndicator
/**
 * Constructor with `k` = 2.
 *
 * @param bbm the middle band Indicator. Typically an `SMAIndicator`
 * is used.
 * @param deviation the deviation above and below the middle, factored by k.
 * Typically a `StandardDeviationIndicator` is used.
 */
@JvmOverloads constructor(
    private val bbm: BollingerBandsMiddleIndicator,
    private val deviation: NumericIndicator,
    private val k: Num = deviation.numFactory.two(),
) : NumericIndicator(deviation.numFactory) {

    private fun calculate() = bbm.value + (deviation.value * k)

    override fun updateState(bar: Bar) {
        bbm.onBar(bar)
        deviation.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = bbm.isStable && deviation.isStable

    override val lag: Int
        get() = maxOf(bbm.lag, deviation.lag)

    override fun toString() = "BolBaUp(${bbm}, ${k}) => $value"
}
