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
package org.ta4j.core.backtest.criteria.pnl

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.num.NumFactory

internal class GrossAverageProfitCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithProfitPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)

        // First trade: buy at 100, sell at 110 (profit of 10)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(110.0)

        // Second trade: buy at 200, sell at 230 (profit of 30)
        context.enter(1.0).at(200.0)
            .exit(1.0).at(230.0)

        context.withCriterion(GrossAverageProfitCriterion())
            .assertResults(20.0) // (10 + 30) / 2 = 20
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)

        // First trade: buy at 100, sell at 90 (loss)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(90.0)

        // Second trade: buy at 200, sell at 180 (loss)
        context.enter(1.0).at(200.0)
            .exit(1.0).at(180.0)

        context.withCriterion(GrossAverageProfitCriterion())
            .assertResults(0.0) // No winning positions, no profit to average
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithMixedProfitAndLoss(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)

        // Profitable trade: buy at 100, sell at 120 (profit of 20)
        context.enter(1.0).at(100.0)
            .exit(1.0).at(120.0)

        // Loss trade: buy at 200, sell at 180 (loss, ignored)
        context.enter(1.0).at(200.0)
            .exit(1.0).at(180.0)

        // Another profitable trade: buy at 300, sell at 340 (profit of 40)
        context.enter(1.0).at(300.0)
            .exit(1.0).at(340.0)

        context.withCriterion(GrossAverageProfitCriterion())
            .assertResults(30.0) // (20 + 40) / 2 = 30
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOneOpenPosition(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.BUY)

        // Open position
        context.enter(1.0).at(100.0)

        context.withCriterion(GrossAverageProfitCriterion())
            .assertResults(0.0) // Open position contributes 0
    }
}