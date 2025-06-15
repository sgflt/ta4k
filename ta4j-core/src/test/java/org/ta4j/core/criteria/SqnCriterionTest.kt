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
package org.ta4j.core.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.criteria.SqnCriterion
import org.ta4j.core.num.NumFactory

class SqnCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithWinningLongPositions(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)

        val tradingContext = context.toTradingRecordContext()
            .withCriterion(SqnCriterion())
            .enter(1.0).asap()
            .exit(1.0).after(2)
            .enter(1.0).asap()
            .exit(1.0).after(2)

        tradingContext.assertResults(4.242640687119286)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithLosingLongPositions(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)

        val tradingContext = context.toTradingRecordContext()
            .withCriterion(SqnCriterion())
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).after(3)

        tradingContext.assertResults(-1.9798989873223332)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOneWinningAndOneLosingLongPositions(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 195.0, 100.0, 80.0, 85.0, 70.0)

        val tradingContext = context.toTradingRecordContext()
            .withCriterion(SqnCriterion())
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).after(3)

        tradingContext.assertResults(0.7353910524340095)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithWinningShortPositions(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 90.0, 100.0, 95.0, 95.0, 100.0)

        val tradingContext = context.toTradingRecordContext()
            .withTradeType(TradeType.SELL)
            .withCriterion(SqnCriterion())
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).asap()

        tradingContext.assertResults(4.242640687119286)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithLosingShortPositions(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 110.0, 100.0, 105.0, 95.0, 105.0)

        val tradingContext = context.toTradingRecordContext()
            .withTradeType(TradeType.SELL)
            .withCriterion(SqnCriterion())
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).asap()

        tradingContext.assertResults(-4.242640687119286)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOneOpenPositionShouldReturnZero(factory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(factory)
            .withCandlePrices(100.0, 105.0)

        val tradingContext = context.toTradingRecordContext()
            .withCriterion(SqnCriterion())
            .enter(1.0).asap()

        tradingContext.assertResults(0.0)
    }
}