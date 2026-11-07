/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.num.NumFactory

class ProfitLossPercentageCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithWinningLongPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ProfitLossPercentageCriterion())

        // First trade: buy at 100, sell at 110 (profit: +10%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        // Second trade: buy at 100, sell at 105 (profit: +5%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(105.0)

        // Total percentage profit should be 10% + 5% = 15%
        context.assertResults(7.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithLosingLongPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ProfitLossPercentageCriterion())

        // First trade: buy at 100, sell at 95 (loss: -5%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(95.0)

        // Second trade: buy at 100, sell at 70 (loss: -30%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(70.0)

        // Total percentage loss should be (-5 - 30) / (100 + 100)
        context.assertResults(-17.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOneWinningAndOneLosingLongPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ProfitLossPercentageCriterion())

        // First trade: buy at 100, sell at 195 (profit: +95%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(195.0)

        // Second trade: buy at 100, sell at 70 (loss: -30%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(70.0)

        // Net percentage should be (95 - 30) / (100 + 100)
        context.assertResults(32.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithWinningShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(ProfitLossPercentageCriterion())

        // First trade: sell at 100, buy at 90 (profit: +10%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(90.0)

        // Second trade: sell at 100, buy at 95 (profit: +5%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(95.0)

        // Total percentage profit should be (10 + 5) / (100 + 100)
        context.assertResults(7.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithLosingShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(ProfitLossPercentageCriterion())

        // First trade: sell at 100, buy at 110 (loss: -10%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        // Second trade: sell at 100, buy at 105 (loss: -5%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(105.0)

        // Total percentage loss should be -10% + -5% = -15%
        context.assertResults(-7.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOneOpenPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ProfitLossPercentageCriterion())

        // Open position without closing it
        context.enter(1.0).at(100.0)

        // Open position should return 0
        context.assertResults(0.0)
    }
}
