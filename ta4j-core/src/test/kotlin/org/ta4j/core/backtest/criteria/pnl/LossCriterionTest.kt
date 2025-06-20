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
import org.ta4j.core.backtest.analysis.cost.LinearTransactionCostModel
import org.ta4j.core.num.NumFactory

class LossCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateComparingIncludingVsExcludingCosts(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withTransactionCostModel(LinearTransactionCostModel(0.01))

        // First trade: buy at 100, sell at 95
        context.enter(1.0).at(100.0)
            .exit(1.0).at(95.0)

        // Second trade: buy at 100, sell at 70
        context.enter(1.0).at(100.0)
            .exit(1.0).at(70.0)

        // Calculate with costs included
        context.withCriterion(LossCriterion(false))
            .assertResults(-38.65)

        // Calculate with costs excluded
        context.withCriterion(LossCriterion(true))
            .assertResults(-35.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithProfitPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(LossCriterion(true))

        // First trade: buy at 100, sell at 110
        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        // Second trade: buy at 100, sell at 105
        context.enter(1.0).at(100.0)
            .exit(1.0).at(105.0)

        context.assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(LossCriterion(true))

        // First trade: buy at 100, sell at 95
        context.enter(1.0).at(100.0)
            .exit(1.0).at(95.0)

        // Second trade: buy at 100, sell at 70
        context.enter(1.0).at(100.0)
            .exit(1.0).at(70.0)

        context.assertResults(-35.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateProfitWithShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(LossCriterion(true))

        // First trade: sell at 95, buy at 100
        context.enter(1.0).at(95.0)
            .exit(1.0).at(100.0)

        // Second trade: sell at 70, buy at 100
        context.enter(1.0).at(70.0)
            .exit(1.0).at(100.0)

        context.assertResults(-35.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOneOpenPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)
            .withCriterion(LossCriterion(true))

        // Open position without closing it
        context.enter(1.0).at(100.0)

        context.assertResults(0.0)
    }
}
