/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Lukáš Kvídera
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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class WMAIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun calculate(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().closePrice(1.0).add()
                    .candle().closePrice(2.0).add()
                    .candle().closePrice(3.0).add()
                    .candle().closePrice(4.0).add()
                    .candle().closePrice(5.0).add()
                    .candle().closePrice(6.0).add()
                    .build()
            )

        val wmaIndicator = WMAIndicator(closePrice(), 3)

        context.withIndicator(wmaIndicator)
            .fastForwardUntilStable()
            .assertCurrent(2.3333)
            .assertNext(3.3333)
            .assertNext(4.3333)
            .assertNext(5.3333)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun wmaWithBarCountGreaterThanSeriesSize(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().closePrice(1.0).add()
                    .candle().closePrice(2.0).add()
                    .candle().closePrice(3.0).add()
                    .candle().closePrice(4.0).add()
                    .candle().closePrice(5.0).add()
                    .candle().closePrice(6.0).add()
                    .build()
            )

        val wmaIndicator = WMAIndicator(closePrice(), 55)

        context.withIndicator(wmaIndicator)
            .fastForward(6)

        assert(!wmaIndicator.isStable)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun wmaUsingBarCount9UsingClosePrice(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                84.53, 87.39, 84.55, 82.83, 82.58, 83.74, 83.33, 84.57, 86.98, 87.10, 83.11, 83.60, 83.66,
                82.76, 79.22, 79.03, 78.18, 77.42, 74.65, 77.48, 76.87
            )

        val wmaIndicator = WMAIndicator(closePrice(), 9)

        context.withIndicator(wmaIndicator)
            .fastForwardUntilStable()
            .assertCurrent(84.4958)
            .assertNext(85.0158)
            .assertNext(84.6807)
            .assertNext(84.5387)
            .assertNext(84.4298)
            .assertNext(84.1224)
            .assertNext(83.1031)
            .assertNext(82.1462)
            .assertNext(81.1149)
            .assertNext(80.0736)
            .assertNext(78.6907)
            .assertNext(78.1504)
            .assertNext(77.6133)
    }
}
