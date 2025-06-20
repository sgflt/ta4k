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
package org.ta4j.core.indicators.numeric.average

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

internal class HMAIndicatorTest {

    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(
            84.53, 87.39, 84.55, 82.83, 82.58, 83.74, 83.33, 84.57, 86.98, 87.10, 83.11, 83.60, 83.66,
            82.76, 79.22, 79.03, 78.18, 77.42, 74.65, 77.48, 76.87
        )
    }

    @ParameterizedTest(name = "HMA [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun hmaUsingBarCount9UsingClosePrice(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)
        // Example from
        // http://traders.com/Documentation/FEEDbk_docs/2010/12/TradingIndexesWithHullMA.xls
        val hma = Indicators.closePrice().hma(9)
        testContext.withIndicator(hma)
            .fastForwardUntilStable()
            .assertCurrent(86.1990)
            .assertNext(86.5763)
            .assertNext(86.3204)
            .assertNext(85.3705)
            .assertNext(84.1044)
            .assertNext(83.0197)
            .assertNext(81.3913)
            .assertNext(79.6511)
            .assertNext(78.0443)
            .assertNext(76.8832)
            .assertNext(75.5363)
            .assertNext(75.1713)
            .assertNext(75.3597)
    }
}
