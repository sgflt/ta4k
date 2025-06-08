/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
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
package org.ta4j.core.strategy.rules

import org.ta4j.core.TradeType
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

/**
 * A take-profit rule (also known as stop-gain rule).
 *
 * Satisfied when indicator reaches the gain threshold relative to the entry price.
 * For BUY positions: triggers when current price >= entry price * (1 + gain%)
 * For SELL positions: triggers when current price <= entry price * (1 - gain%)
 *
 * @param indicator the indicator
 * @param gainPercentage the gain percentage (e.g., 10.0 for 10% gain)
 * @param runtimeContext the runtime context to access current position information
 */
class TakeProfitRule(
    private val indicator: NumericIndicator,
    gainPercentage: Double,
    private val runtimeContext: RuntimeContext,
) : AbstractRule() {

    private val gainPercentage: Num = indicator.numFactory.numOf(gainPercentage)
    private val hundred = indicator.numFactory.hundred()

    /**
     * Constructor with Num gain percentage.
     */
    constructor(
        indicator: NumericIndicator,
        gainPercentage: Num,
        runtimeContext: RuntimeContext,
    ) : this(indicator, gainPercentage.doubleValue(), runtimeContext)

    init {
        require(gainPercentage > 0.0) { "gainPercentage must be positive" }
    }

    /**
     * Resolver to get the current entry price from runtime context.
     */
    private val entryPriceResolver = RuntimeValueResolver<Num?> { context ->
        context.getValue("currentEntryPrice") as? Num
    }

    /**
     * Resolver to get the current trade type from runtime context.
     */
    private val tradeTypeResolver = RuntimeValueResolver<TradeType?> { context ->
        context.getValue("currentTradeType") as? TradeType
    }

    override val isSatisfied: Boolean
        get() {
            val entryPrice = runtimeContext.getValue(entryPriceResolver)
            val tradeType = runtimeContext.getValue(tradeTypeResolver)

            if (entryPrice == null || tradeType == null) {
                val satisfied = false
                traceIsSatisfied(satisfied)
                return satisfied
            }

            val currentPrice = indicator.value

            val satisfied = when (tradeType) {
                TradeType.BUY -> isBuyGainSatisfied(entryPrice, currentPrice)
                TradeType.SELL -> isSellGainSatisfied(entryPrice, currentPrice)
            }

            traceIsSatisfied(satisfied)
            return satisfied
        }

    private fun isBuyGainSatisfied(entryPrice: Num, currentPrice: Num): Boolean {
        val gainMultiplier = (hundred + gainPercentage) / hundred
        val threshold = entryPrice * gainMultiplier
        return currentPrice >= threshold
    }

    private fun isSellGainSatisfied(entryPrice: Num, currentPrice: Num): Boolean {
        val gainMultiplier = (hundred - gainPercentage) / hundred
        val threshold = entryPrice * gainMultiplier
        return currentPrice <= threshold
    }

    override fun toString(): String {
        return "TakeProfitRule[${gainPercentage.doubleValue()}%] => $isSatisfied"
    }
}
