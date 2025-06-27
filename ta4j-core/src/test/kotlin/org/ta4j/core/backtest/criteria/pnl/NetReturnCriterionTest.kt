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
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.num.NumFactory

class NetReturnCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testNetReturnCriterionWithBase(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0)

        // Net return with base should be 1.1 (10% gain + 100% base)
        marketContext.toTradingRecordContext()
            .withCriterion(NetReturnCriterion(true))
            .enter(100.0).asap()
            .exit(100.0).asap()
            .assertResults(1.1)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testNetReturnCriterionWithoutBase(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0)

        // Net return without base should be 0.1 (10% gain only)
        marketContext.toTradingRecordContext()
            .withCriterion(NetReturnCriterion(false))
            .enter(100.0).asap()
            .exit(100.0).asap()
            .assertResults(0.1)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testNetReturnCriterionForMultiplePositions(numFactory: NumFactory) {
        val marketContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 105.0, 115.5)

        // Two positions: 10% gain, then 10% gain = 1.1 * 1.1 = 1.21
        marketContext.toTradingRecordContext()
            .withCriterion(NetReturnCriterion(true))
            .enter(100.0).asap()
            .exit(100.0).asap()
            .enter(100.0).asap()
            .exit(100.0).asap()
            .assertResults(1.21)
    }
}