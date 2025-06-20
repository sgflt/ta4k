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
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.indicators.XLSIndicatorTest
import org.ta4j.core.num.NumFactory

internal class SMAIndicatorTest {
    private lateinit var testContext: MarketEventTestContext


    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 4.0, 3.0, 3.0, 4.0, 3.0, 2.0)
    }


    @ParameterizedTest(name = "SMA 3 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun usingBarCount3UsingClosePrice(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val indicator = closePrice().sma(3)
        testContext.withIndicators(indicator)
            .assertNext((0.0 + 0.0 + 1.0) / 3)
            .assertNext((0.0 + 1.0 + 2.0) / 3)
            .assertNext((1.0 + 2.0 + 3.0) / 3)
            .assertNext(3.0)
            .assertNext(10.0 / 3)
            .assertNext(11.0 / 3)
            .assertNext(4.0)
            .assertNext(13.0 / 3)
            .assertNext(4.0)
            .assertNext(10.0 / 3)
            .assertNext(10.0 / 3)
            .assertNext(10.0 / 3)
            .assertNext(3.0)
    }


    @ParameterizedTest(name = "SMA 1 [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun whenBarCountIs1ResultShouldBeIndicatorValue(numFactory: NumFactory) {
        this.testContext.withNumFactory(numFactory)
        val closePrice = closePrice()
        val indicator = closePrice.sma(1)

        this.testContext.withIndicators(indicator)
            .assertIndicatorEquals(closePrice, indicator)
    }


    @ParameterizedTest(name = "SMA 3 external data [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun externalData3(numFactory: NumFactory) {
        externalData(numFactory, 326.6333, 3)
    }


    @ParameterizedTest(name = "SMA 13 external data [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun externalData13(numFactory: NumFactory) {
        externalData(numFactory, 327.7846, 13)
    }


    private fun externalData(numFactory: NumFactory, expectedLastValue: Double, barCount: Int) {
        val xlsContext = MarketEventTestContext()
        xlsContext.withNumFactory(numFactory)
        val xls = XLSIndicatorTest(javaClass, "SMA.xls", 6, numFactory)
        xlsContext.withMarketEvents(xls.getMarketEvents())

        val actualIndicator = closePrice().sma(barCount)
        val expectedIndicator = xls.getIndicator(barCount)
        xlsContext.withIndicators(actualIndicator, expectedIndicator)
            .fastForwardUntilStable()
            .assertIndicatorEquals(expectedIndicator, actualIndicator)
            .assertCurrent(expectedLastValue)
    }
}
