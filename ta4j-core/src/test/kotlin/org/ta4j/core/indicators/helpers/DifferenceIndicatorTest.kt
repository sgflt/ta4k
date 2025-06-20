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
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class DifferenceIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun indicatorShouldRetrieveBarDifference(numFactory: NumFactory) {
        val closePriceIndicator = Indicators.closePrice()
        val closePriceDifference = DifferenceIndicator(closePriceIndicator)

        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 15.0, 14.0, 13.0, 16.0, 18.0, 17.0, 19.0)
            .withIndicator(closePriceDifference)
            .assertNextNaN()  // First value is NaN (no previous value)
            .assertNext(1.0)  // 11 - 10 = 1
            .assertNext(1.0)  // 12 - 11 = 1
            .assertNext(3.0)  // 15 - 12 = 3
            .assertNext(-1.0) // 14 - 15 = -1
            .assertNext(-1.0) // 13 - 14 = -1
            .assertNext(3.0)  // 16 - 13 = 3
            .assertNext(2.0)  // 18 - 16 = 2
            .assertNext(-1.0) // 17 - 18 = -1
    }
}