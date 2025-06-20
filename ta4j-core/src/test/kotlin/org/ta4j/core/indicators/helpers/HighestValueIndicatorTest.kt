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
import org.ta4j.core.indicators.numeric.helpers.HighestValueIndicator
import org.ta4j.core.num.NumFactory

class HighestValueIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun highestValueUsingBarCount5UsingClosePrice(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 6.0, 4.0, 3.0, 3.0, 4.0, 3.0, 2.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val highestValue = HighestValueIndicator(numFactory, closePrice, 5)

        context
            .withIndicator(highestValue)
            .assertNext(1.0)  // index 0: only value 1
            .assertNext(2.0)  // index 1: highest of [1, 2] = 2
            .assertNext(3.0)  // index 2: highest of [1, 2, 3] = 3
            .assertNext(4.0)  // index 3: highest of [1, 2, 3, 4] = 4
            .assertNext(4.0)  // index 4: highest of [1, 2, 3, 4, 3] = 4
            .assertNext(4.0)  // index 5: highest of [2, 3, 4, 3, 4] = 4
            .assertNext(5.0)  // index 6: highest of [3, 4, 3, 4, 5] = 5
            .assertNext(6.0)  // index 7: highest of [4, 3, 4, 5, 6] = 6
            .assertNext(6.0)  // index 8: highest of [3, 4, 5, 6, 4] = 6
            .assertNext(6.0)  // index 9: highest of [4, 5, 6, 4, 3] = 6
            .assertNext(6.0)  // index 10: highest of [5, 6, 4, 3, 3] = 6
            .assertNext(6.0)  // index 11: highest of [6, 4, 3, 3, 4] = 6
            .assertNext(4.0)  // index 12: highest of [4, 3, 3, 4, 3] = 4
            .assertNext(4.0)  // index 13: highest of [3, 3, 4, 3, 2] = 4
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun firstHighestValueIndicatorValueShouldBeEqualsToFirstDataValue(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 6.0, 4.0, 3.0, 3.0, 4.0, 3.0, 2.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val highestValue = HighestValueIndicator(numFactory, closePrice, 5)

        context
            .withIndicator(highestValue)
            .assertNext(1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun highestValueIndicatorWhenBarCountIsGreaterThanIndex(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 6.0, 4.0, 3.0, 3.0, 4.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val highestValue = HighestValueIndicator(numFactory, closePrice, 500)

        context
            .withIndicator(highestValue)
            .assertNext(1.0)
            .assertNext(2.0)
            .assertNext(3.0)
            .assertNext(4.0)
            .assertNext(4.0)
            .assertNext(4.0)
            .assertNext(5.0)
            .assertNext(6.0)
            .assertNext(6.0)
            .assertNext(6.0)
            .assertNext(6.0)
            .assertNext(6.0)
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
        val highestValue = HighestValueIndicator(numFactory, closePrice, 5)

        context.withIndicator(highestValue)

        // All values should be NaN
        repeat(10) {
            context.assertNextNaN()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun naNValuesInInterval(numFactory: NumFactory) {
        // Create alternating pattern: 0, NaN, 2, NaN, 4, NaN, 6, NaN, 8, NaN, 10
        val prices = (0..10).map { i ->
            if (i % 2 == 0) i.toDouble() else Double.NaN
        }.toDoubleArray()

        val closePrice = Indicators.extended(numFactory).closePrice()
        val highestValue = HighestValueIndicator(numFactory, closePrice, 2)

        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*prices)
            .withIndicator(highestValue)
            .assertNext(0.0)    // index 0: only value 0
            .assertNext(0.0)    // index 1: highest of [0, NaN] = 0
            .assertNext(2.0)    // index 2: highest of [NaN, 2] = 2
            .assertNext(2.0)    // index 3: highest of [2, NaN] = 2
            .assertNext(4.0)    // index 4: highest of [NaN, 4] = 4
            .assertNext(4.0)    // index 5: highest of [4, NaN] = 4
            .assertNext(6.0)    // index 6: highest of [NaN, 6] = 6
            .assertNext(6.0)    // index 7: highest of [6, NaN] = 6
            .assertNext(8.0)    // index 8: highest of [NaN, 8] = 8
            .assertNext(8.0)    // index 9: highest of [8, NaN] = 8
            .assertNext(10.0)   // index 10: highest of [NaN, 10] = 10
    }
}
