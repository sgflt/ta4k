/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.momentum

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * The RandomWalkIndexHighIndicator.
 *
 * Implements the RWI High calculation according to E. Michael Poulos's original methodology.
 * This indicator calculates the maximum relative displacement across multiple timeframes
 * and compares it to what would be expected in a random walk.
 */
class RWILowIndicator(
    numFactory: NumFactory,
    private val barCount: Int = 14,
) : RWIIndicator(numFactory, barCount) {

    private val lowPrice = LowPriceIndicator(numFactory)
    private val highPrice = HighPriceIndicator(numFactory)

    // Cache of previous low values
    private val previsouHighs =
        Array(barCount) { i -> PreviousNumericValueIndicator(numFactory, highPrice, i + 1) }

    override fun getHigh(period: Int): Num {
        return previsouHighs[period].value
    }

    override fun getLow(period: Int): Num {
        return lowPrice.value
    }

    override fun updateState(bar: Bar) {
        super.updateState(bar)
        lowPrice.onBar(bar)
        highPrice.onBar(bar)
        previsouHighs.forEach { it.onBar(bar) }

        if (isStable) {
            value = calculate()
        }
    }

    override fun toString(): String {
        return "RWI Low ${super.toString()}"
    }
}
