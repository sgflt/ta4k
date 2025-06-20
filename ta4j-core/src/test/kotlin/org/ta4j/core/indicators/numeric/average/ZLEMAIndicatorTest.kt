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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.num.NumFactory

class ZLEMAIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun zLEMAUsingBarCount10UsingClosePrice(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                10.0, 15.0, 20.0, 18.0, 17.0, 18.0, 15.0, 12.0, 10.0, 8.0, 5.0, 2.0
            )

        val zlema = ZLEMAIndicator(closePrice(), 10)

        context.withIndicator(zlema)
            .fastForwardUntilStable()
            .assertNext(4.2727)
            .assertNext(8.7685)
            .assertNext(14.2652)
            .assertNext(18.0351)
            .assertNext(19.1196)
            .assertNext(19.4615)
            .assertNext(17.7412)
            .assertNext(15.6065)
            .assertNext(13.3144)
            .assertNext(10.5299)
            .assertNext(7.70634)
            .assertNext(4.8506)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun zLEMAFirstValueShouldBeEqualsToFirstDataValue(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 15.0, 20.0, 18.0, 17.0, 18.0, 15.0, 12.0, 10.0, 8.0, 5.0, 2.0)

        val zlema = ZLEMAIndicator(closePrice(), 10)

        context.withIndicator(zlema)
            .assertNext(10.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun smallBarCount(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 15.0, 20.0, 18.0, 17.0, 18.0, 15.0, 12.0, 10.0, 8.0, 5.0, 2.0)

        val zlema = ZLEMAIndicator(closePrice(), 3)

        context.withIndicator(zlema)
            .assertNext(10.0)
    }
}
