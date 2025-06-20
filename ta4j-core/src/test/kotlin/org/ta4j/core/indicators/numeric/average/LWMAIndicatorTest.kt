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

internal class LWMAIndicatorTest {

    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(37.08, 36.7, 36.11, 35.85, 35.71, 36.04, 36.41, 37.67, 38.01, 37.79, 36.83)
    }

    @ParameterizedTest(name = "LWMA [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun lwmaUsingBarCount5UsingClosePrice(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val lwma = Indicators.closePrice().lwma(5)

        testContext.withIndicator(lwma)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(0.0)
            .assertNext(36.0506)
            .assertNext(35.9673)
            .assertNext(36.0766)
            .assertNext(36.6253)
            .assertNext(37.1833)
            .assertNext(37.5240)
            .assertNext(37.4060)
    }
}
