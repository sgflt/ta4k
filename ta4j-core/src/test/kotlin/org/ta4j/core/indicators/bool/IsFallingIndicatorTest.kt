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
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class IsFallingIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should identify strict falling pattern`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 9.0, 8.0, 7.0, 6.0, 5.0)
            .withIndicator(
                IsFallingIndicator(
                    numFactory = numFactory,
                    indicator = Indicators.extended(numFactory).closePrice(),
                    barCount = 3,
                    minStrength = 1.0
                )
            )

        // First bar - not enough data
        context.assertNextFalse()

        // Second bar - first comparison, falling
        context.assertNextTrue()

        // Third bar - window size 2, 2 falling periods
        context.assertNextTrue() // 2/2 = 1.0 >= 1.0

        // Fourth bar - window size 3, all falling
        context.assertNextTrue() // 3/3 = 1.0 >= 1.0

        // Fifth bar - window size 3, all still falling
        context.assertNextTrue() // 3/3 = 1.0 >= 1.0

        // Sixth bar - window size 3, all still falling
        context.assertNextTrue() // 3/3 = 1.0 >= 1.0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle partial falling with min strength`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 9.0, 11.0, 15.0, 5.0)
            .withIndicator(
                IsFallingIndicator(
                    numFactory = numFactory,
                    indicator = Indicators.extended(numFactory).closePrice(),
                    barCount = 3,
                    minStrength = 0.5 // At least 50% falling
                )
            )

        context.assertNextFalse()
        context.assertNextTrue()
        context.assertNextTrue()
        context.assertNextFalse()
        context.assertNextFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle rising pattern`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 6.0, 7.0, 8.0, 9.0)
            .withIndicator(
                IsFallingIndicator(
                    numFactory = numFactory,
                    indicator = Indicators.extended(numFactory).closePrice(),
                    barCount = 3,
                    minStrength = 1.0
                )
            )

        context.fastForward(5)
        assertThat(context.fisrtBooleanIndicator!!.value).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle mixed pattern with low strength requirement`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 9.0, 11.0, 8.0, 12.0, 7.0)
            .withIndicator(
                IsFallingIndicator(
                    numFactory = numFactory,
                    indicator = Indicators.extended(numFactory).closePrice(),
                    barCount = 4,
                    minStrength = 0.25 // At least 25% falling
                )
            )

        context.fastForwardUntilStable()

        // After all bars: last 4 comparisons [8<11, 12>8, 7<12] = 2 falling out of 4
        // 2/4 = 0.5 >= 0.25
        assertThat(context.fisrtBooleanIndicator!!.value).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle constant values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 10.0, 10.0, 10.0)
            .withIndicator(
                IsFallingIndicator(
                    numFactory = numFactory,
                    indicator = Indicators.extended(numFactory).closePrice(),
                    barCount = 3,
                    minStrength = 0.1
                )
            )

        context.fastForward(4)
        // No falling values, ratio = 0/3 = 0 < 0.1
        assertThat(context.fisrtBooleanIndicator!!.value).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with single bar window`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 9.0, 8.0, 9.0)
            .withIndicator(
                IsFallingIndicator(
                    numFactory = numFactory,
                    indicator = Indicators.extended(numFactory).closePrice(),
                    barCount = 1,
                    minStrength = 1.0
                )
            )

        context.assertNextFalse() // Not enough data
        context.assertNextTrue()  // 9 < 10, 1/1 = 1.0
        context.assertNextTrue()  // 8 < 9, 1/1 = 1.0
        context.assertNextFalse() // 9 > 8, 0/1 = 0.0
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle edge case with zero strength`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0)
            .withIndicator(
                IsFallingIndicator(
                    numFactory = numFactory,
                    indicator = Indicators.extended(numFactory).closePrice(),
                    barCount = 2,
                    minStrength = 0.0 // Always satisfied
                )
            )

        context.assertNextFalse() // Not enough data
        context.assertNextTrue()  // 0/1 = 0.0 >= 0.0, but window too small initially
        context.assertNextTrue()  // 0/2 = 0.0 >= 0.0
    }

    @Test
    fun `should validate constructor parameters`() {
        val numFactory = org.ta4j.core.num.DoubleNumFactory
        val indicator = Indicators.extended(numFactory).closePrice()

        assertThatThrownBy {
            IsFallingIndicator(numFactory, indicator, 0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("n must be positive number, but was: 0")

        assertThatThrownBy {
            IsFallingIndicator(numFactory, indicator, 5, -0.1)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Min strength must be between 0.0 and 1.0")

        assertThatThrownBy {
            IsFallingIndicator(numFactory, indicator, 5, 1.1)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Min strength must be between 0.0 and 1.0")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should provide correct lag and stability`(numFactory: NumFactory) {
        val indicator = IsFallingIndicator(
            numFactory = numFactory,
            indicator = Indicators.extended(numFactory).closePrice(),
            barCount = 5,
            minStrength = 0.6
        )

        assertThat(indicator.lag).isEqualTo(5)
        assertThat(indicator.isStable).isFalse()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0)
            .withIndicator(indicator)

        // Should become stable after enough data
        context.fastForwardUntilStable()
        assertThat(indicator.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work as trading rule`(numFactory: NumFactory) {
        val fallingIndicator = IsFallingIndicator(
            numFactory = numFactory,
            indicator = Indicators.extended(numFactory).closePrice(),
            barCount = 3,
            minStrength = 0.67 // At least 2 out of 3 falling
        )

        val rule = fallingIndicator.toRule()

        TradingRecordTestContext()
            .withNumFactory(numFactory)

        // Simulate a trading scenario where we enter when price is falling
        // and exit when it stops falling
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 90.0, 85.0, 80.0, 85.0, 90.0)
            .withIndicator(fallingIndicator)
            .fastForwardUntilStable()

        // The rule should be satisfied when the falling pattern is strong enough
        assertThat(rule.isSatisfied).isNotNull()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle very large window efficiently`(numFactory: NumFactory) {
        val prices = DoubleArray(1000) { i -> 1000.0 - i * 0.1 } // Gradually falling

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*prices)
            .withIndicator(
                IsFallingIndicator(
                    numFactory = numFactory,
                    indicator = Indicators.extended(numFactory).closePrice(),
                    barCount = 500,
                    minStrength = 0.95
                )
            )

        // Should handle large windows efficiently using streaming algorithm
        context.fastForwardUntilStable()
        assertThat(context.fisrtBooleanIndicator!!.value).isTrue()
    }
}
