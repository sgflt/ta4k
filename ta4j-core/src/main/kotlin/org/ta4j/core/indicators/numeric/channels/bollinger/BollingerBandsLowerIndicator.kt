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
class BollingerBandsLowerIndicator @JvmOverloads constructor(
    private val bbm: BollingerBandsMiddleIndicator,
    private val indicator: NumericIndicator,
    private val k: Num = indicator.numFactory.two(),
) : NumericIndicator(indicator.numFactory) {

    private fun calculate() = bbm.value - (indicator.value * k)

    override fun updateState(bar: Bar) {
        bbm.onBar(bar)
        indicator.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = indicator.isStable && bbm.isStable

    override val lag: Int
        get() = maxOf(bbm.lag, indicator.lag)

    override fun toString() = "BolBaLo(${bbm}, ${k}) => $value"
}
