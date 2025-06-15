/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.backtest.criteria.pnl

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.num.NumFactory

class ProfitLossRatioCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithProfitPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ProfitLossRatioCriterion())

        // First trade: buy at 100, sell at 120 (profit: 20)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(120.0)

        // Second trade: buy at 120, sell at 130 (profit: 10)
        context.enter(1.0).at(120.0)
            .exit(1.0).at(130.0)

        // Only profits, no losses, so ratio should be 1
        context.assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ProfitLossRatioCriterion())

        // First trade: buy at 100, sell at 95 (loss: -5)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(95.0)

        // Second trade: buy at 100, sell at 70 (loss: -30)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(70.0)

        // Only losses, no profits, so ratio should be 0
        context.assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateProfitWithShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(ProfitLossRatioCriterion())

        // First trade: sell at 100, buy at 85 (profit: +15)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(85.0)

        // Second trade: sell at 80, buy at 95 (loss: -15)
        context.enter(1.0).at(80.0)
            .exit(1.0).at(95.0)

        // Profit/Loss ratio = Total Profit / Total Loss = 15/15 = 1
        context.assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateMixedProfitsAndLosses(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ProfitLossRatioCriterion())

        // First trade: buy at 100, sell at 150 (profit: +50)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(150.0)

        // Second trade: buy at 100, sell at 80 (loss: -20)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(80.0)

        // Third trade: buy at 100, sell at 120 (profit: +20)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(120.0)

        // Fourth trade: buy at 100, sell at 90 (loss: -10)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(90.0)

        // Total profits = 50 + 20 = 70
        // Total losses = 20 + 10 = 30
        // Profit/Loss ratio = 70/30 = 2.333...
        context.assertResults(2.333333333333333)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOneOpenPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(ProfitLossRatioCriterion())

        // Open position without closing it
        context.enter(1.0).at(100.0)

        // Open position should return 0
        context.assertResults(0.0)
    }
}
