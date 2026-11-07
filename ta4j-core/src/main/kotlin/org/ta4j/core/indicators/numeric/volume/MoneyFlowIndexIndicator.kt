/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.VolumeIndicator
import org.ta4j.core.indicators.numeric.candles.price.TypicalPriceIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.utils.CircularNumArray

/**
 * Money Flow Index (MFI) indicator.
 *
 * MFI is a volume-weighted version of RSI that shows shifts in buying and
 * selling pressure. It uses both price and volume to measure buying and selling
 * pressure. The MFI oscillates between 0 and 100, where readings above 80 are
 * considered overbought and readings below 20 are considered oversold.
 *
 * For more information, see:
 * https://school.stockcharts.com/doku.php?id=technical_indicators:money_flow_index_mfi
 *
 * @param numFactory the number factory for creating Num instances
 * @param barCount the time frame (number of bars to consider)
 */
class MoneyFlowIndexIndicator(
    numFactory: NumFactory,
    private val barCount: Int,
) : NumericIndicator(numFactory) {

    private val typicalPrice = TypicalPriceIndicator(numFactory)
    private val volume = VolumeIndicator(numFactory)
    private var previousTypicalPrice = typicalPrice.previous()
    private val positiveFlows = CircularNumArray(barCount)
    private val negativeFlows = CircularNumArray(barCount)

    init {
        require(barCount > 0) { "Bar count must be positive, but was: $barCount" }
    }

    override val lag = barCount

    override fun updateState(bar: Bar) {
        typicalPrice.onBar(bar)
        volume.onBar(bar)
        previousTypicalPrice.onBar(bar)

        val currentTypicalPrice = typicalPrice.value
        val currentVolume = volume.value
        val rawMoneyFlow = currentTypicalPrice * currentVolume

        when {
            !previousTypicalPrice.isStable -> {
                // First bar - no comparison possible, add zero to both
                positiveFlows.addLast(numFactory.zero())
                negativeFlows.addLast(numFactory.zero())
            }

            currentTypicalPrice > previousTypicalPrice.value -> {
                // Typical price increased - positive money flow
                positiveFlows.addLast(rawMoneyFlow)
                negativeFlows.addLast(numFactory.zero())
            }

            currentTypicalPrice < previousTypicalPrice.value -> {
                // Typical price decreased - negative money flow
                positiveFlows.addLast(numFactory.zero())
                negativeFlows.addLast(rawMoneyFlow)
            }

            else -> {
                // Prices are equal - no money flow
                positiveFlows.addLast(numFactory.zero())
                negativeFlows.addLast(numFactory.zero())
            }
        }

        value = calculate()
    }

    private fun calculate(): Num {
        if (positiveFlows.isEmpty || positiveFlows.isNotFull) {
            return NaN
        }

        var sumPositive = numFactory.zero()
        var sumNegative = numFactory.zero()

        for (flow in positiveFlows) {
            flow.let { sumPositive += it }
        }

        for (flow in negativeFlows) {
            flow.let { sumNegative += it }
        }

        // Calculate money flow ratio using manual max logic to prevent division by zero
        // This matches the original Java implementation exactly
        val positiveMax = if (sumPositive >= numFactory.one()) sumPositive else numFactory.one()
        val negativeMax = if (sumNegative >= numFactory.one()) sumNegative else numFactory.one()
        val moneyFlowRatio = positiveMax / negativeMax

        // Calculate MFI using the standard formula
        return numFactory.hundred() - (numFactory.hundred() / (numFactory.one() + moneyFlowRatio))

    }

    override val isStable: Boolean
        get() = !positiveFlows.isEmpty && !positiveFlows.isNotFull

    override fun toString(): String {
        return "MFI($barCount) => $value"
    }
}
