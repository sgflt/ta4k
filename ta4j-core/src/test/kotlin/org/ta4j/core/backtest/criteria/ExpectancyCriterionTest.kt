/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.num.NumFactory

class ExpectancyCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithProfitPositions(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(ExpectancyCriterion())

        // First trade: buy at 100, sell at 120 (profit: +20%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(120.0)

        // Second trade: buy at 130, sell at 160 (profit: +23%)
        context.enter(1.0).at(130.0)
            .exit(1.0).at(160.0)

        // All trades are profitable, expectancy should be 1.0
        context.assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithMixedPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ExpectancyCriterion())

        // First trade: buy at 100, sell at 80 (loss: -20%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(80.0)

        // Second trade: buy at 130, sell at 160 (profit: +23%)
        context.enter(1.0).at(130.0)
            .exit(1.0).at(160.0)

        // One winning trade and one losing trade
        // Expectancy = (1 winning trade / 2 total trades) = 0.25
        context.assertResults(0.25)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ExpectancyCriterion())

        // First trade: buy at 100, sell at 95 (loss: -5%)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(95.0)

        // Second trade: buy at 80, sell at 50 (loss: -37.5%)
        context.enter(1.0).at(80.0)
            .exit(1.0).at(50.0)

        // All trades are losses, expectancy should be 0
        context.assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateProfitWithShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(ExpectancyCriterion())

        // First trade: sell at 160, buy at 140 (profit: +12.5%)
        context.enter(1.0).at(160.0)
            .exit(1.0).at(140.0)

        // Second trade: sell at 120, buy at 60 (profit: +50%)
        context.enter(1.0).at(120.0)
            .exit(1.0).at(60.0)

        // All trades are profitable, expectancy should be 1.0
        context.assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateProfitWithMixedShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(ExpectancyCriterion())

        // First trade: sell at 160, buy at 200 (loss: -25%)
        context.enter(1.0).at(160.0)
            .exit(1.0).at(200.0)

        // Second trade: sell at 120, buy at 60 (profit: +50%)
        context.enter(1.0).at(120.0)
            .exit(1.0).at(60.0)

        // One winning trade and one losing trade
        // Expectancy = (1 winning trade / 2 total trades) = 0.25
        context.assertResults(0.25)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOneOpenPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ExpectancyCriterion())

        // Open position without closing it
        context.enter(1.0).at(100.0)

        // Open position should return 0
        context.assertResults(0.0)
    }
}
