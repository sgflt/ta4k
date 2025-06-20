/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.bool

import java.time.Instant
import org.ta4j.core.TradeType
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

/**
 * A trailing stop-loss indicator.
 *
 * Returns true when the price reaches the trailing loss threshold based on the highest/lowest
 * price since position entry (or within the specified barCount window).
 *
 * @param priceIndicator the price indicator (typically close price)
 * @param lossPercentage the loss percentage threshold
 * @param barCount the maximum number of bars to look back (default: unlimited)
 * @param runtimeContext the runtime context to access position information
 */
class TrailingStopLossIndicator(
    private val priceIndicator: NumericIndicator,
    private val lossPercentage: Num,
    private val barCount: Int,
    private val runtimeContext: RuntimeContext,
) : BooleanIndicator() {

    private val hundred = priceIndicator.numFactory.hundred()
    private val highestIndicator = priceIndicator.highest(barCount)
    private val lowestIndicator = priceIndicator.lowest(barCount)

    /**
     * Data class to represent position information from runtime context
     */
    @JvmRecord
    data class PositionInfo(
        val isOpen: Boolean,
        val tradeType: TradeType,
        val entryTime: Instant,
        val entryPrice: Num,
        val barsInPosition: Int,
    )

    private val positionResolver = RuntimeValueResolver<PositionInfo?> { context ->
        context.getValue("TSLcurrentPosition") as? PositionInfo
    }

    override fun updateState(bar: Bar) {
        highestIndicator.onBar(bar)
        lowestIndicator.onBar(bar)

        value = calculate()
    }

    private fun calculate(): Boolean {
        val currentPrice = priceIndicator.value

        // Get position information from runtime context
        val positionInfo = runtimeContext?.getValue(positionResolver)
            ?: return false // No position open

        if (!positionInfo.isOpen) {
            return false // No position open
        }

        return when (positionInfo.tradeType) {
            TradeType.BUY -> isBuyPositionStopLossTriggered(currentPrice)
            TradeType.SELL -> isSellPositionStopLossTriggered(currentPrice)
        }
    }

    /**
     * Check if trailing stop loss is triggered for a BUY position.
     * For BUY positions, we track the highest price and stop out if price falls below
     * (highest * (100 - lossPercentage) / 100).
     */
    private fun isBuyPositionStopLossTriggered(currentPrice: Num): Boolean {
        val highestPrice = highestIndicator.value

        // Calculate stop loss threshold: highest * (100 - lossPercentage) / 100
        val lossRatioThreshold = (hundred - lossPercentage) / hundred
        val stopLossThreshold = highestPrice * lossRatioThreshold

        return currentPrice <= stopLossThreshold
    }

    /**
     * Check if trailing stop loss is triggered for a SELL position.
     * For SELL positions, we track the lowest price and stop out if price rises above
     * (lowest * (100 + lossPercentage) / 100).
     */
    private fun isSellPositionStopLossTriggered(currentPrice: Num): Boolean {
        val lowestPrice = lowestIndicator.value

        // Calculate stop loss threshold: lowest * (100 + lossPercentage) / 100
        val lossRatioThreshold = (hundred + lossPercentage) / hundred
        val stopLossThreshold = lowestPrice * lossRatioThreshold

        return currentPrice >= stopLossThreshold
    }

    override val lag: Int
        get() = maxOf(highestIndicator.lag, lowestIndicator.lag)

    override val isStable: Boolean
        get() = priceIndicator.isStable && highestIndicator.isStable && lowestIndicator.isStable

    override fun toString(): String {
        return "TrailingStopLoss[${priceIndicator}, ${lossPercentage}%, barCount=$barCount] => $value"
    }
}
