/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator
import org.ta4j.core.num.NumFactory

class LowestValueIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun lowestValueIndicatorUsingBarCount5UsingClosePrice(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 6.0, 4.0, 3.0, 2.0, 4.0, 3.0, 1.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val lowestValue = LowestValueIndicator(numFactory, closePrice, 5)

        context
            .withIndicator(lowestValue)
            .assertNext(1.0)  // index 0: only value 1
            .assertNext(1.0)  // index 1: lowest of [1, 2] = 1
            .assertNext(1.0)  // index 2: lowest of [1, 2, 3] = 1
            .assertNext(1.0)  // index 3: lowest of [1, 2, 3, 4] = 1
            .assertNext(1.0)  // index 4: lowest of [1, 2, 3, 4, 3] = 1
            .assertNext(2.0)  // index 5: lowest of [2, 3, 4, 3, 4] = 2
            .assertNext(3.0)  // index 6: lowest of [3, 4, 3, 4, 5] = 3
            .assertNext(3.0)  // index 7: lowest of [4, 3, 4, 5, 6] = 3
            .assertNext(3.0)  // index 8: lowest of [3, 4, 5, 6, 4] = 3
            .assertNext(3.0)  // index 9: lowest of [4, 5, 6, 4, 3] = 3
            .assertNext(2.0)  // index 10: lowest of [5, 6, 4, 3, 2] = 2
            .assertNext(2.0)  // index 11: lowest of [6, 4, 3, 2, 4] = 2
            .assertNext(2.0)  // index 12: lowest of [4, 3, 2, 4, 3] = 2
            .assertNext(1.0)  // index 13: lowest of [3, 2, 4, 3, 1] = 1
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun lowestValueIndicatorValueShouldBeEqualsToFirstDataValue(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 6.0, 4.0, 3.0, 2.0, 4.0, 3.0, 1.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val lowestValue = LowestValueIndicator(numFactory, closePrice, 5)

        context
            .withIndicator(lowestValue)
            .assertNext(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun lowestValueIndicatorWhenBarCountIsGreaterThanIndex(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 6.0, 4.0, 3.0, 2.0, 4.0, 3.0, 1.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val lowestValue = LowestValueIndicator(numFactory, closePrice, 500)

        context
            .withIndicator(lowestValue)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)
            .assertNext(1.0)  // All values should be 1 (the minimum overall)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun onlyNaNValues(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        // Create series with NaN close prices
        val closePrices = DoubleArray(10) { Double.NaN }
        context.withCandlePrices(*closePrices)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val lowestValue = LowestValueIndicator(numFactory, closePrice, 5)

        context.withIndicator(lowestValue)

        // All values should be NaN
        repeat(10) {
            context.assertNextNaN()
        }
    }
}
