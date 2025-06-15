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

class ProfitLossCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithProfitPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(ProfitLossCriterion())

        context.enter(50.0).at(100.0)
            .exit(50.0).at(110.0)
            .enter(50.0).at(100.0)
            .exit(50.0).at(105.0)
            .assertResults(500.0 + 250.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withCriterion(ProfitLossCriterion())

        context.enter(50.0).at(100.0)
            .exit(50.0).at(95.0)
            .enter(50.0).at(100.0)
            .exit(50.0).at(70.0)
            .assertResults(-250.0 - 1500.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithProfitShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(ProfitLossCriterion())

        context.enter(50.0).at(100.0)
            .exit(50.0).at(110.0)
            .enter(50.0).at(100.0)
            .exit(50.0).at(105.0)
            .assertResults(-(500.0 + 250.0))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossShortPositions(numFactory: NumFactory) {
        val context = TradingRecordTestContext()
            .withNumFactory(numFactory)
            .withTradeType(TradeType.SELL)
            .withCriterion(ProfitLossCriterion())

        context.enter(50.0).at(100.0)
            .exit(50.0).at(95.0)
            .enter(50.0).at(100.0)
            .exit(50.0).at(70.0)
            .assertResults(250.0 + 1500.0)
    }
}
