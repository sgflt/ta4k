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
package org.ta4j.core.indicators.bool

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class IsLowestIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be true when current value is lowest in window`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 2.0, 3.0, 1.0, 4.0)  // Trough at index 3
            .withIndicator(Indicators.extended(numFactory).closePrice().isLowest(3))

        // Advance to get enough data
        context.fastForward(2)  // Move to index 2 (value 3.0)
        context.assertNextFalse()  // 3.0 is not lowest in [5.0, 2.0, 3.0]

        // Move to index 3 (value 1.0)
        context.assertNextTrue()   // 1.0 is lowest in [2.0, 3.0, 1.0]

        // Move to index 4 (value 4.0)
        context.assertNextFalse()  // 1.0 is still lowest in [3.0, 1.0, 4.0]
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle tie for lowest value`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 1.0, 3.0, 1.0)  // Two troughs with same value
            .withIndicator(Indicators.extended(numFactory).closePrice().isLowest(3))

        context.fastForward(3)  // Move to index 3 (value 1.0)

        // Current value (1.0) equals the lowest in window [1.0, 3.0, 1.0]
        context.assertNextTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single bar window`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 2.0, 8.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().isLowest(1))

        // With window of 1, current value is always the lowest in its window
        repeat(3) {
            context.assertNextTrue()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle descending sequence`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 4.0, 3.0, 2.0, 1.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().isLowest(3))

        repeat(5) {
            context.assertNextTrue()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle ascending sequence`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().isLowest(3))

        context.assertNextTrue()   // 1.0 is lowest in [1.0, 2.0, 3.0]
        repeat(4) {
            context.assertNextFalse()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle window larger than available data`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(3.0, 1.0, 2.0)
            .withIndicator(Indicators.extended(numFactory).closePrice())

        val closePrice = context.firstNumericIndicator!!
        val indicator = IsLowestIndicator(closePrice, 10)  // Window larger than data
        context.withIndicator(indicator)

        context.advance()  // Move to index 0 (value 3.0)
        assertThat(indicator.value).isTrue()  // Only value available

        context.advance()  // Move to index 1 (value 1.0)
        assertThat(indicator.value).isTrue()  // 1.0 is lowest in [3.0, 1.0]

        context.advance()  // Move to index 2 (value 2.0)
        assertThat(indicator.value).isFalse()  // 1.0 is lowest in [3.0, 1.0, 2.0]
    }

    @Test
    fun `should use correct string representation`() {
        val context = MarketEventTestContext()
            .withCandlePrices(1.0, 2.0, 3.0)
            .withIndicator(Indicators.closePrice())

        val closePrice = context.firstNumericIndicator!!
        val indicator = IsLowestIndicator(closePrice, 5)

        assertThat(indicator.toString()).contains("IsLowest")
        assertThat(indicator.toString()).contains("5")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle flat sequence with same values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(3.0, 3.0, 3.0, 3.0, 3.0)
            .withIndicator(Indicators.extended(numFactory).closePrice())

        val closePrice = context.firstNumericIndicator!!
        val indicator = IsLowestIndicator(closePrice, 3)
        context.withIndicator(indicator)

        // All values are equal, so current should always be lowest (tied)
        while (context.advance()) {
            assertThat(indicator.value).isTrue()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle mixed positive and negative values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(2.0, 1.0, -1.0, 0.0, 3.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().isLowest(3))

        context.fastForward(2)  // Move to index 2 (value -1.0)
        context.assertNextTrue()  // -1.0 is lowest in [2.0, 1.0, -1.0]

        // Move to index 3 (value 0.0)
        context.assertNextFalse()  // -1.0 is still lowest in [1.0, -1.0, 0.0]

        // Move to index 4 (value 3.0)
        context.assertNextFalse()  // -1.0 is still lowest in [-1.0, 0.0, 3.0]
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should convert to rule for strategy usage`(numFactory: NumFactory) {
        val indicator = Indicators.extended(numFactory).closePrice().isLowest(3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 1.0, 3.0, 2.0)
            .withIndicator(indicator)

        // Convert to rule for use in strategies
        val rule = indicator.toRule()

        context.fastForward(2)
        assertThat(rule.isSatisfied).isTrue()

        context.advance()
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should test lag and stability properties`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 4.0, 3.0, 2.0, 1.0)
            .withIndicator(Indicators.extended(numFactory).closePrice())

        val closePrice = context.firstNumericIndicator!!
        val indicator = IsLowestIndicator(closePrice, 3)

        // Lag should match the lowest value indicator's lag
        assertThat(indicator.lag).isEqualTo(3)

        context.withIndicator(indicator)

        // Should become stable after enough bars
        context.fastForwardUntilStable()
        assertThat(indicator.isStable).isTrue()
    }
}
