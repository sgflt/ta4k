/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.statistics

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class MeanDeviationIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `mean deviation using bar count 5 using close price`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 7.0, 6.0, 3.0, 4.0, 5.0, 11.0, 3.0, 0.0, 9.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().meanDeviation(5))

        // Fast forward to index 4 (5th bar) where mean deviation calculation starts
        context.fastForward(4)
        // Index 4: window [1,2,7,6,3], mean=3.8, mean deviation=2.16
        context.assertNext(2.16)
        // Index 5: window [2,7,6,3,4], mean=4.4, mean deviation=1.68
        context.assertNext(1.68)
        // Index 6: window [7,6,3,4,5], mean=5.0, mean deviation=1.2
        context.assertNext(1.2)
        // Index 7: window [6,3,4,5,11], mean=5.8, mean deviation=2.16
        context.assertNext(2.16)
        // Index 8: window [3,4,5,11,3], mean=5.2, mean deviation=2.42
        context.assertNext(2.32)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `first value should be NaN`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val meanDeviation = MeanDeviationIndicator(closePrice, 5)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 7.0, 6.0, 3.0)
            .withIndicator(meanDeviation)

        context.assertNextNaN()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `mean deviation should be zero when bar count is 1`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val meanDeviation = MeanDeviationIndicator(closePrice, 1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 7.0)
            .withIndicator(meanDeviation)

        context.assertNext(0.0)
        context.assertNext(0.0)
        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `mean deviation calculation with different bar counts`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val meanDeviation3 = MeanDeviationIndicator(closePrice, 3)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 8.0, 15.0, 5.0)
            .withIndicator(meanDeviation3)

        context.fastForwardUntilStable()

        // Fourth value: mean of [12, 8, 15] = 11.67, deviations = [0.33, 3.67, 3.33], mean deviation ≈ 2.44444
        context.assertNext(2.44444444444444)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `indicator should be stable after sufficient bars`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val meanDeviation = MeanDeviationIndicator(closePrice, 3)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0)
            .withIndicator(meanDeviation)

        // Should not be stable initially
        assertThat(meanDeviation.isStable).isFalse()

        // Advance to get enough data
        context.fastForward(3)

        // Should be stable now
        assertThat(meanDeviation.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `lag should equal bar count`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val meanDeviation5 = MeanDeviationIndicator(closePrice, 5)
        val meanDeviation10 = MeanDeviationIndicator(closePrice, 10)

        assertThat(meanDeviation5.lag).isEqualTo(5)
        assertThat(meanDeviation10.lag).isEqualTo(10)
    }
}
