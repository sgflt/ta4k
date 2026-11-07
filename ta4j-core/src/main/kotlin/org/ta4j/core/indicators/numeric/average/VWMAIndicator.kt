/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.operation.BinaryOperation
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Volume Weighted Moving Average (VWMA) indicator.
 *
 * Volume Weighted Moving Average (VWMA) is a type of moving average where
 * each data point's weight is dynamically adjusted based on market conditions
 * or other factors such as price, volume, or volatility. This makes the VWMA a
 * powerful tool for analyzing trends in varying market conditions, offering
 * both responsiveness and stability.
 *
 * VWMA_t = (Sum(Weight_i * Price_i)) / (Sum(Weight_i))
 *
 * @property priceIndicator price based indicator
 * @property barCount the time frame
 */
class VWMAIndicator(
    numFactory: NumFactory,
    private val priceIndicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val volumeIndicator = Indicators.extended(numFactory).volume()
    private val weighteedPriceSum = BinaryOperation.product(priceIndicator, volumeIndicator)
    private val volumeWeightedIndicator = BinaryOperation.quotient(
        weighteedPriceSum.sma(barCount),
        volumeIndicator.sma(barCount)
    );

    private fun calculate(): Num = volumeWeightedIndicator.value

    override fun updateState(bar: Bar) {
        volumeWeightedIndicator.onBar(bar)
        value = calculate()
    }


    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = volumeWeightedIndicator.isStable

    override fun toString(): String {
        return "VWMA($barCount) => $value"
    }
}
