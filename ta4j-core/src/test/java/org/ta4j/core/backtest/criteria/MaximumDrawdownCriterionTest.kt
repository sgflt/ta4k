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
import org.ta4j.core.TradeType
import org.ta4j.core.num.NumFactory

class MaximumDrawdownCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithNoTrades(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0)
        marketContext.toTradingRecordContext()
            .withCriterion(MaximumDrawdownCriterion(numFactory))
            .assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithOnlyGains(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(2.0, 3.0, 3.0, 5.0, 10.0, 20.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(MaximumDrawdownCriterion(numFactory))
            .enter(1.0).after(1)
            .exit(1.0).after(1)
            .enter(1.0).after(2)  // skip 3.0
            .exit(1.0).after(2)    // skip 10.0
            .assertResults(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithGainsAndLosses(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 20.0, 1.0, 8.0, 3.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(MaximumDrawdownCriterion(numFactory))
            .enter(1.0).after(1)
            .exit(1.0).after(5)
            .assertResults((20.0 - 1.0) / 20.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateWithSimpleTrades(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 10.0, 10.0, 5.0, 5.0, 6.0, 6.0, 1.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(MaximumDrawdownCriterion(numFactory))
            .enter(1.0).after(1)
            .exit(1.0).after(1)
            .enter(1.0).after(1)
            .exit(1.0).after(1)
            .enter(1.0).after(1)
            .exit(1.0).after(1)
            .enter(1.0).after(1)
            .exit(1.0).after(1)
            .assertResults((6.0 - 1.0) / 6.0) // maximum drawdown within last trade
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculateHODL(numFactory: NumFactory) {
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 10.0, 10.0, 5.0, 5.0, 6.0, 6.0, 1.0)
            .toTradingRecordContext()
            .withTradeType(TradeType.BUY)
            .withCriterion(MaximumDrawdownCriterion(numFactory))
            .enter(1.0).after(1)
            .exit(1.0).after(7)
            .assertResults(0.9)  // 10.0 -> 1.0 which is -90 %
    }
}
