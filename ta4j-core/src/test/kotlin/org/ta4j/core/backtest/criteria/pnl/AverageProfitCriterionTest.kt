/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.num.NumFactory

class AverageProfitCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithProfitPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(AverageProfitCriterion())

        // First trade: buy at 100, sell at 110 (profit: +10)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        // Second trade: buy at 100, sell at 105 (profit: +5)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(105.0)

        // Average profit should be (10 + 5) / 2 = 7.5
        context.assertResults(7.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(AverageProfitCriterion())

        // First trade: buy at 100, sell at 95 (loss: -5)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(95.0)

        // Second trade: buy at 100, sell at 70 (loss: -30)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(70.0)

        // No profits, so average profit should be 0
        context.assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateProfitWithShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(AverageProfitCriterion())

        // First trade: sell at 100, buy at 85 (profit: +15)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(85.0)

        // Second trade: sell at 80, buy at 95 (loss: -15)
        context.enter(1.0).at(80.0)
            .exit(1.0).at(95.0)

        // Average profit should be (15 + 0) / 2 = 15
        context.assertResults(15.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOneOpenPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(AverageProfitCriterion())

        // Open position without closing it
        context.enter(1.0).at(100.0)

        // Open position should return 0
        context.assertResults(0.0)
    }
}
