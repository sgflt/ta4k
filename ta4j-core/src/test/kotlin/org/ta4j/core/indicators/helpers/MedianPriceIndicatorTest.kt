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
package org.ta4j.core.indicators.helpers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.numeric.candles.price.MedianPriceIndicator
import org.ta4j.core.num.NumFactory

class MedianPriceIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun indicatorShouldRetrieveBarMedianPrice(numFactory: NumFactory) {
        // For simplicity, use withCandlePrices which sets OHLC to the same value
        // Since MedianPriceIndicator calculates (high + low) / 2, and high = low = price
        // The median price will be (price + price) / 2 = price
        val medianPriceIndicator = MedianPriceIndicator(numFactory)

        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 15.0, 25.0)
            .withIndicator(medianPriceIndicator)
            .assertNext(10.0)  // (10 + 10) / 2 = 10
            .assertNext(20.0)  // (20 + 20) / 2 = 20
            .assertNext(30.0)  // (30 + 30) / 2 = 30
            .assertNext(15.0)  // (15 + 15) / 2 = 15
            .assertNext(25.0)  // (25 + 25) / 2 = 25
    }
}
