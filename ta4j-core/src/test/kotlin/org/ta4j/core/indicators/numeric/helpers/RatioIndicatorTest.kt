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
package org.ta4j.core.indicators.numeric.helpers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class RatioIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate ratio between current and previous values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 15.0, 30.0, 25.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val ratioIndicator = RatioIndicator(closePrice)

        context.withIndicator(ratioIndicator)

        // First value should be the price itself (10.0) - previous indicator not stable yet
        context.assertNext(10.0)
        assertUnstable(ratioIndicator)

        // Second value should be 20.0 / 10.0 = 2.0 - now stable
        context.assertNext(2.0)
        assertStable(ratioIndicator)

        // Third value should be 15.0 / 20.0 = 0.75
        context.assertNext(0.75)

        // Fourth value should be 30.0 / 15.0 = 2.0
        context.assertNext(2.0)

        // Fifth value should be 25.0 / 30.0 = 0.8333...
        context.assertNext(0.8333333333333334)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle division by zero with NaN`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(0.0, 10.0, 0.0, 5.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val ratioIndicator = RatioIndicator(closePrice)

        context.withIndicator(ratioIndicator)

        // First value should be 0.0 (previous indicator not stable yet)
        context.assertNext(0.0)

        // Second value: 10.0 / 0.0 should be 0 (now stable)
        context.advance()
        assertThat(ratioIndicator.value).isEqualTo(numFactory.zero())

        // Third value: 0.0 / 10.0 = 0.0
        context.assertNext(0.0)

        // Fourth value: 5.0 / 0.0 should be NaN
        context.advance()
        assertThat(ratioIndicator.value).isEqualTo(numFactory.zero())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag and stability behavior`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 15.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val ratioIndicator = RatioIndicator(closePrice)

        context.withIndicator(ratioIndicator)

        // Should not be stable initially
        assertUnstable(ratioIndicator)
        assertThat(ratioIndicator.lag).isEqualTo(1)

        // After first bar, still not stable (previous indicator needs 1 more bar)
        context.advance()
        assertUnstable(ratioIndicator)

        // After second bar, should be stable
        context.advance()
        assertStable(ratioIndicator)

        // Should remain stable
        context.advance()
        assertStable(ratioIndicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle negative values correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(-10.0, -20.0, 5.0, -2.5)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val ratioIndicator = RatioIndicator(closePrice)

        context.withIndicator(ratioIndicator)

        // First value should be -10.0
        context.assertNext(-10.0)

        // Second value: -20.0 / -10.0 = 2.0
        context.assertNext(2.0)

        // Third value: 5.0 / -20.0 = -0.25
        context.assertNext(-0.25)

        // Fourth value: -2.5 / 5.0 = -0.5
        context.assertNext(-0.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single bar correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(42.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val ratioIndicator = RatioIndicator(closePrice)

        context.withIndicator(ratioIndicator)

        // Single value should just return the value itself (not stable yet)
        context.assertNext(42.0)
        assertUnstable(ratioIndicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle very small values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(0.001, 0.002, 0.0005)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val ratioIndicator = RatioIndicator(closePrice)

        context.withIndicator(ratioIndicator)

        // First value should be 0.001
        context.assertNext(0.001)

        // Second value: 0.002 / 0.001 = 2.0
        context.assertNext(2.0)

        // Third value: 0.0005 / 0.002 = 0.25
        context.assertNext(0.25)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should become stable after receiving two bars`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val ratioIndicator = RatioIndicator(closePrice)

        context.withIndicator(ratioIndicator)

        // Initially unstable
        assertUnstable(ratioIndicator)

        // After first bar: still unstable (PreviousNumericValueIndicator needs 2 bars)
        context.advance()
        assertThat(ratioIndicator.value.doubleValue()).isEqualTo(1.0)
        assertUnstable(ratioIndicator)

        // After second bar: now stable and can calculate ratios
        context.advance()
        assertThat(ratioIndicator.value.doubleValue()).isEqualTo(2.0) // 2.0 / 1.0
        assertStable(ratioIndicator)

        // Third bar: remains stable
        context.advance()
        assertThat(ratioIndicator.value.doubleValue()).isEqualTo(1.5) // 3.0 / 2.0
        assertStable(ratioIndicator)
    }
}
