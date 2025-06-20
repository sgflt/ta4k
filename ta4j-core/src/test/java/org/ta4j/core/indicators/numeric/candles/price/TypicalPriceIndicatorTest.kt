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

package org.ta4j.core.indicators.numeric.candles.price

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class TypicalPriceIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun indicatorShouldRetrieveTypicalPrice(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()

        val typicalPriceIndicator = Indicators.extended(numFactory).typicalPrice()
        context.withIndicator(typicalPriceIndicator)

        while (context.advance()) {
            val bar = context.barSeries.bar
            val expectedTypicalPrice = (bar.highPrice + bar.lowPrice + bar.closePrice) / numFactory.numOf(3)
            context.assertCurrent(expectedTypicalPrice.doubleValue())
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldCalculateTypicalPriceWithSpecificValues(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                // Close prices
                10.0, 20.0, 15.0, 30.0, 25.0
            )

        // Manually create bars with different H, L, C values for more realistic testing
        // Using close price for all OHLC to simplify, but the calculation will be (H+L+C)/3
        val typicalPriceIndicator = Indicators.extended(numFactory).typicalPrice()
        context.withIndicator(typicalPriceIndicator)

        // Since we're using close price for all OHLC, typical price should equal close price
        // (C+C+C)/3 = C
        context.assertNext(10.0)
        context.assertNext(20.0)
        context.assertNext(15.0)
        context.assertNext(30.0)
        context.assertNext(25.0)
    }
}