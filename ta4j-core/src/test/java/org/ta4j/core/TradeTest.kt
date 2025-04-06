/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.backtest.Trade
import org.ta4j.core.backtest.Trade.OrderType
import org.ta4j.core.backtest.analysis.cost.LinearTransactionCostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.num.DoubleNum
import org.ta4j.core.num.NumFactory
import java.time.Instant

internal class TradeTest {
    @Test
    fun buyTransactionShouldIncreaseNetPriceByTransactionCosts() {
        val transactionCostModel = LinearTransactionCostModel(0.1) // 10% fee
        val trade = Trade(
            type = TradeType.BUY,
            orderType = OrderType.OPEN,
            whenExecuted = Instant.EPOCH,
            pricePerAsset = DoubleNum.valueOf(100),
            amount = DoubleNum.valueOf(1),
            transactionCostModel = transactionCostModel
        )

        assertNumEquals(100, trade.pricePerAsset) // Original price
        assertNumEquals(110, trade.netPrice) // Price + 10% fee
        assertNumEquals(10, trade.cost) // Transaction cost
    }


    @Test
    fun sellTransactionShouldDecreaseNetPriceByTransactionCosts() {
        val transactionCostModel = LinearTransactionCostModel(0.1) // 10% fee
        val trade = Trade(
            type = TradeType.SELL,
            orderType = OrderType.CLOSE,
            whenExecuted = Instant.EPOCH,
            pricePerAsset = DoubleNum.valueOf(100),
            amount = DoubleNum.valueOf(1),
            transactionCostModel = transactionCostModel
        )

        assertNumEquals(100, trade.pricePerAsset) // Original price
        assertNumEquals(90, trade.netPrice) // Price - 10% fee
        assertNumEquals(10, trade.cost) // Transaction cost
    }


    @Test
    fun tradingValueShouldBeBasedOnPriceAndAmount() {
        val trade = Trade(
            type = TradeType.BUY,
            transactionCostModel = ZeroCostModel,
            orderType = OrderType.OPEN,
            whenExecuted = Instant.EPOCH,
            pricePerAsset = DoubleNum.valueOf(100),
            amount = DoubleNum.valueOf(2)
        )

        // Value = price * amount (without transaction costs)
        assertNumEquals(200, trade.value)
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun costsShouldScaleWithTradeSize(numFactory: NumFactory) {
        val transactionCostModel = LinearTransactionCostModel(0.01) // 1% fee

        // Small trade
        val smallTrade = Trade(
            whenExecuted = Instant.EPOCH,
            pricePerAsset = numFactory.numOf(100),
            type = TradeType.BUY,
            orderType = OrderType.OPEN,
            amount = numFactory.numOf(1),
            transactionCostModel = transactionCostModel
        )

        // Large trade - 10x size
        val largeTrade = Trade(
            whenExecuted = Instant.EPOCH,
            pricePerAsset = numFactory.numOf(100),
            type = TradeType.BUY,
            orderType = OrderType.OPEN,
            amount = numFactory.numOf(10),
            transactionCostModel = transactionCostModel
        )

        // Cost should scale linearly with amount
        assertNumEquals(largeTrade.cost, smallTrade.cost.multipliedBy(numFactory.numOf(10)))
    }
}
