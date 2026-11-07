/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import java.time.Instant
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
        assertNumEquals(largeTrade.cost, smallTrade.cost * numFactory.numOf(10))
    }
}
