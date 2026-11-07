/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

class BollingerBandsMiddleIndicator(private val indicator: NumericIndicator) : NumericIndicator(indicator.numFactory) {
    private fun calculate() = indicator.value

    public override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = indicator.isStable

    override val lag: Int
        get() = indicator.lag

    override fun toString() = "BolBaMid(${indicator}) => $value"
}
