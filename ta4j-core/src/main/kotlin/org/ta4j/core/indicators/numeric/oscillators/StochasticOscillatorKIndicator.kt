/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Stochastic oscillator K.
 */
class StochasticOscillatorKIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    barCount: Int,
    highPriceIndicator: HighPriceIndicator,
    lowPriceIndicator: LowPriceIndicator,
) : NumericIndicator(numFactory) {
    private val highestHigh = highPriceIndicator.highest(barCount)
    private val lowestMin = lowPriceIndicator.lowest(barCount)


    /**
     * Constructor with:
     *
     *
     *  * `indicator` = [ClosePriceIndicator]
     *  * `highPriceIndicator` = [HighPriceIndicator]
     *  * `lowPriceIndicator` = [LowPriceIndicator]
     *
     *
     * @param numFactory the bar series
     * @param barCount the time frame
     */
    constructor(numFactory: NumFactory, barCount: Int) : this(
        numFactory,
        Indicators.extended(numFactory).closePrice(),
        barCount,
        Indicators.extended(numFactory).highPrice(),
        Indicators.extended(numFactory).lowPrice()
    )


    private fun calculate(): Num {
        val highestHighPrice = highestHigh.value
        val lowestLowPrice = lowestMin.value

        val fullCandleHeight = highestHighPrice.minus(lowestLowPrice)
        if (fullCandleHeight.isZero) {
            return numFactory.fifty()
        }

        return ((indicator.value - lowestLowPrice) / fullCandleHeight) * numFactory.hundred()
    }


    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        highestHigh.onBar(bar)
        lowestMin.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = indicator.isStable && highestHigh.isStable && lowestMin.isStable

    override val lag: Int
        get() = highestHigh.lag
}
