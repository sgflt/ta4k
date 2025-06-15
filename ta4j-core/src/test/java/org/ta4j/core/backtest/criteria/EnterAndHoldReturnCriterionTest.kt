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
package org.ta4j.core.backtest.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.num.NumFactory

class EnterAndHoldReturnCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithEmpty(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices()

        val buyContext = marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.buy(marketContext.barSeries))

        val sellContext = marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.sell(marketContext.barSeries))

        buyContext.assertResults(1.0)
        sellContext.assertResults(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithGainPositions(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)

        // Process all market events to populate the bar series
        while (marketContext.advance()) {
            // Process all events
        }

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.buy(marketContext.barSeries))
            .enter(1.0).at(100.0)
            .exit(1.0).at(110.0)
            .enter(1.0).at(100.0)
            .exit(1.0).at(105.0)
            .assertResults(1.05)

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.sell(marketContext.barSeries))
            .enter(1.0).at(100.0)
            .exit(1.0).at(110.0)
            .enter(1.0).at(100.0)
            .exit(1.0).at(105.0)
            .assertResults(0.95)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateOnlyWithLossPositions(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)

        // Process all market events to populate the bar series
        while (marketContext.advance()) {
            // Process all events
        }

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.buy(marketContext.barSeries))
            .enter(1.0).at(100.0)
            .exit(1.0).at(95.0)
            .enter(1.0).at(100.0)
            .exit(1.0).at(70.0)
            .assertResults(0.7)

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.sell(marketContext.barSeries))
            .enter(1.0).at(100.0)
            .exit(1.0).at(95.0)
            .enter(1.0).at(100.0)
            .exit(1.0).at(70.0)
            .assertResults(1.3)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoPositions(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)

        // Advance market context to populate all bars
        while (marketContext.advance()) {
            // Process all market events
        }
        
        val buyContext = marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.buy(marketContext.barSeries))

        buyContext.assertResults(0.7)

        val sellContext = marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.sell(marketContext.barSeries))

        sellContext.assertResults(1.3)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOnePosition(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 95.0)

        // Process all market events to populate the bar series
        while (marketContext.advance()) {
            // Process all events
        }

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.buy(marketContext.barSeries))
            .enter(1.0).at(105.0)
            .exit(1.0).at(95.0)
            .assertResults(0.95)

        marketContext.toTradingRecordContext()
            .withCriterion(EnterAndHoldReturnCriterion.sell(marketContext.barSeries))
            .enter(1.0).at(105.0)
            .exit(1.0).at(95.0)
            .assertResults(1.05)
    }
}