/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.num.NumFactory

class AverageLossCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithProfitPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(AverageLossCriterion())

        // First trade: buy at 100, sell at 110 (profit: +10)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        // Second trade: buy at 100, sell at 105 (profit: +5)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(105.0)

        // No losses, so average loss should be 0
        context.assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(AverageLossCriterion())

        // First trade: buy at 100, sell at 95 (loss: -5)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(95.0)

        // Second trade: buy at 100, sell at 70 (loss: -30)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(70.0)

        // Average loss should be (-5 + -30) / 2 = -17.5
        context.assertResults(-17.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateProfitWithShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(AverageLossCriterion())

        // First trade: sell at 95, buy at 100 (loss: -5)
        context.enter(1.0).at(95.0)
            .exit(1.0).at(100.0)

        // Second trade: sell at 70, buy at 100 (loss: -30)
        context.enter(1.0).at(70.0)
            .exit(1.0).at(100.0)

        // Average loss should be (-5 + -30) / 2 = -17.5
        context.assertResults(-17.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOneOpenPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(AverageLossCriterion())

        // Open position without closing it
        context.enter(1.0).at(100.0)

        // Open position should return 0
        context.assertResults(0.0)
    }
}
