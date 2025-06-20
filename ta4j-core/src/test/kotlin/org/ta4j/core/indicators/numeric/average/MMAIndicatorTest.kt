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
import org.ta4j.core.indicators.XLSIndicatorTest
import org.ta4j.core.num.NumFactory

internal class MMAIndicatorTest {

    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(
            64.75,
            63.79,
            63.73,
            63.73,
            63.55,
            63.19,
            63.91,
            63.85,
            62.95,
            63.37,
            61.33,
            61.51
        )
    }

    @ParameterizedTest(name = "MMA 1 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun firstValueShouldBeEqualsToFirstDataValue(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val actualIndicator = Indicators.closePrice().mma(1)

        testContext.withIndicator(actualIndicator)
            .assertNext(64.75)
    }

    @ParameterizedTest(name = "MMA 1 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun mmaUsingBarCount10UsingClosePrice(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val actualIndicator = Indicators.closePrice().mma(10)

        testContext.withIndicator(actualIndicator)
            .fastForwardUntilStable()
            .assertCurrent(63.9983)
            .assertNext(63.7315)
            .assertNext(63.5093)
    }

    @ParameterizedTest(name = "MMA 1 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testAgainstExternalData1(numFactory: NumFactory) {
        assertBarCount(1, 329.0, numFactory)
    }

    @ParameterizedTest(name = "MMA 3 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testAgainstExternalData3(numFactory: NumFactory) {
        assertBarCount(3, 327.2900, numFactory)
    }

    @ParameterizedTest(name = "MMA 13 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testAgainstExternalData13(numFactory: NumFactory) {
        assertBarCount(13, 326.9696, numFactory)
    }

    private fun assertBarCount(barCount: Int, expected: Double, numFactory: NumFactory) {
        val xlsContext = MarketEventTestContext()
        val xls = XLSIndicatorTest(this.javaClass, "MMA.xls", 6, numFactory)
        xlsContext.withMarketEvents(xls.getMarketEvents())
        val actualIndicator = Indicators.closePrice().mma(barCount)
        val expectedIndicator = xls.getIndicator(barCount)
        xlsContext.withIndicators(actualIndicator, expectedIndicator)
            .assertIndicatorEquals(expectedIndicator, actualIndicator)
    }
}
