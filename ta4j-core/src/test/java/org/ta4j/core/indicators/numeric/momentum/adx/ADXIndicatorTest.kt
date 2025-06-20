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
package org.ta4j.core.indicators.numeric.momentum.adx

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.XLSIndicatorTest
import org.ta4j.core.num.NumFactory

internal class ADXIndicatorTest {

    private lateinit var testContext: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
    }

    @ParameterizedTest(name = "ADX (1,1) [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun externalData11(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)
        val xlsTest = XLSIndicatorTest(this::class.java, "ADX.xls", 15, numFactory)

        testContext.withMarketEvents(xlsTest.getMarketEvents())
        val indicator = ADXIndicator(numFactory, 1, 1)

        testContext.withIndicator(indicator)
            .fastForward(xlsTest.getMarketEvents().size - 1)
            .assertCurrent(100.0)
    }

    @ParameterizedTest(name = "ADX (3,2) [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun externalData32(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)
        val xlsTest = XLSIndicatorTest(this::class.java, "ADX.xls", 15, numFactory)

        testContext.withMarketEvents(xlsTest.getMarketEvents())
        val indicator = ADXIndicator(numFactory, 3, 2)
        val expectedIndicator = xlsTest.getIndicator(3, 2)

        testContext.withIndicators(indicator, expectedIndicator)
            .assertIndicatorEquals(expectedIndicator, indicator)
    }

    @ParameterizedTest(name = "ADX (13,8) [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun externalData138(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)
        val xlsTest = XLSIndicatorTest(this::class.java, "ADX.xls", 15, numFactory)

        testContext.withMarketEvents(xlsTest.getMarketEvents())
        val indicator = ADXIndicator(numFactory, 13, 8)
        val expectedIndicator = xlsTest.getIndicator(13, 8)

        testContext.withIndicators(indicator, expectedIndicator)
            .assertIndicatorEquals(expectedIndicator, indicator)
    }
}
