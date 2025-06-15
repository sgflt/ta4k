/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
import org.ta4j.core.backtest.criteria.AverageReturnPerBarCriterion
import org.ta4j.core.num.NumFactory
import java.time.temporal.ChronoUnit
import kotlin.math.pow

class AverageReturnPerBarCriterionTest {

    private val context = MarketEventTestContext()

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithGainPositions(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
            .enter(1.0).asap()
            .exit(1.0).after(2)
            .enter(1.0).asap()
            .exit(1.0).after(2)

        tradingContext.assertResults((110.0 / 100.0 * 105.0 / 100.0).pow(1.0 / 4.0))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithASimplePosition(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0)
            .toTradingRecordContext()
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
            .enter(1.0).asap()
            .exit(1.0).after(2)

        val expectedReturn = numFactory.numOf(110.0).div(numFactory.numOf(100.0))
            .pow(numFactory.numOf(1.0 / 2.0))
        tradingContext.assertResults(expectedReturn.doubleValue())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
            .toTradingRecordContext()
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
            .enter(1.0).asap()
            .exit(1.0).asap()
            .enter(1.0).asap()
            .exit(1.0).after(3)

        val expectedReturn = numFactory.numOf(95.0 / 100.0 * 70.0 / 100.0).pow(numFactory.numOf(1.0 / 4.0))
        tradingContext.assertResults(expectedReturn.doubleValue())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithLosingShortPositions(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 90.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.SELL)
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
            .enter(1.0).asap()
            .exit(1.0).after(2)

        val expectedReturn = numFactory.numOf((100.0 - 90.0) / 100.0 + 1.0).pow(numFactory.numOf(1.0 / 2.0))
        tradingContext.assertResults(expectedReturn.doubleValue())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoBarsShouldReturnOne(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
            .toTradingRecordContext()
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))

        tradingContext.assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOnePosition(numFactory: NumFactory) {
        val tradingContext = context
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(AverageReturnPerBarCriterion(numFactory, ChronoUnit.DAYS))
            .enter(1.0).asap()
            .exit(1.0).asap()

        val expectedReturn = numFactory.numOf(105.0 / 100.0).pow(numFactory.numOf(1.0))
        tradingContext.assertResults(expectedReturn.doubleValue())
    }
}