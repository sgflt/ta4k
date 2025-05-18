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
package org.ta4j.core.indicators.numeric

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.indicators.numeric.momentum.RWILowIndicator
import org.ta4j.core.num.NumFactory

class RWILowIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldCalculateCorrectValues(numFactory: NumFactory) {
        // Given
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(18.0, 16.0, 15.0, 13.0, 14.0, 12.0, 11.0, 10.0)

        // When
        val rwiLow = RWILowIndicator(numFactory, 5)
        context.withIndicator(rwiLow)

        // Then - NaN until stable
        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNextNaN()

        // Then - Real values once stable
        context.assertNext(11.0881)
        context.assertNext(9.3826)
        context.assertNext(9.4999)

        assertStable(rwiLow)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldHandleFlatPrices(numFactory: NumFactory) {
        // Given
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0)

        // When
        val rwiLow = RWILowIndicator(numFactory, 4)
        context.withIndicator(rwiLow)

        // Then - NaN until stable
        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNextNaN()

        // Then - Flat prices should give a value of zero (no trend)
        context.assertNext(0.0)
        context.assertNext(0.0)
        context.assertNext(0.0)

        assertStable(rwiLow)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldHandleStrongUptrend(numFactory: NumFactory) {
        // Given
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(80.0, 70.0, 60.0, 50.0, 40.0, 30.0, 20.0, 10.0)

        // When
        val rwiLow = RWILowIndicator(numFactory, 3)
        context.withIndicator(rwiLow)

        // Then - NaN until stable
        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNextNaN()

        // Then - Linear strong uptrend should give high values
        context.assertNext(6.0290)
        context.assertNext(5.2869)
        context.assertNext(4.8861)
        context.assertNext(4.6509)
        context.assertNext(4.5063)

        assertStable(rwiLow)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldWorkWithShortPeriod(numFactory: NumFactory) {
        // Given
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(25.0, 22.0, 20.0, 19.0, 16.0, 13.0, 14.0, 15.0, 12.0, 10.0)

        // When
        val rwih = RWILowIndicator(numFactory, 2)
        context.withIndicator(rwih)

        // Then - NaN for one bar, then becomes stable
        context.assertNextNaN()
        context.assertNextNaN()
        // Continue testing additional values
        context.assertNext(2.8571)
        assertStable(rwih)
        context.assertNext(2.1818)
        context.assertNext(1.8285)
        context.assertNext(2.3132)
        context.assertNext(1.1130)
        context.assertNext(0.0000)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldWorkWithLongPeriod(numFactory: NumFactory) {
        // Given
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(25.0, 22.0, 20.0, 19.0, 16.0, 13.0, 14.0, 15.0, 12.0, 10.0)

        // When
        val rwih = RWILowIndicator(numFactory, 5)
        context.withIndicator(rwih)

        // Then - NaN until stable (4 bars)
        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNextNaN()

        // Continue testing additional values
        context.assertNext(14.4703)
        assertStable(rwih)
        context.assertNext(10.4791)
        context.assertNext(7.0349)
        context.assertNext(8.0590)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun shouldHandleUptrend(numFactory: NumFactory) {
        // Given
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0)

        // When
        val rwiLow = RWILowIndicator(numFactory, 3)
        context.withIndicator(rwiLow)

        // Then - NaN until ATR is stable (3 bars for period=3)
        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNextNaN()

        context.assertNext(0.0)
        context.assertNext(0.0)
        context.assertNext(0.0)
        context.assertNext(0.0)
        context.assertNext(0.0)

        assertStable(rwiLow)
    }
}
