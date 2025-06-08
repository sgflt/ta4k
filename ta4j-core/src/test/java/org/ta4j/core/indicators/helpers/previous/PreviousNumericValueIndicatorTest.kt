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
package org.ta4j.core.indicators.helpers.previous

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class PreviousNumericValueIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return previous value with n=1`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().previous(1))

        // First value should be NaN (no previous value available)
        context.assertNextNaN()

        // Second value should be the first price (10.0)
        context.assertNext(10.0)

        // Third value should be the second price (20.0)
        context.assertNext(20.0)

        // Fourth value should be the third price (30.0)
        context.assertNext(30.0)

        // Fifth value should be the fourth price (40.0)
        context.assertNext(40.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return previous value with n=3`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0, 60.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().previous(3))

        // First three values should be NaN (not enough previous values)
        repeat(3) {
            context.assertNextNaN()
        }

        // Fourth value should be the first price (10.0)
        context.assertNext(10.0)

        // Fifth value should be the second price (20.0)
        context.assertNext(20.0)

        // Sixth value should be the third price (30.0)
        context.assertNext(30.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle large n value`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().previous(10))

        // All values should be NaN since n=10 > available data
        repeat(5) {
            context.assertNextNaN()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should maintain sliding window correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().previous(2))

        // First two values should be NaN
        context.assertNextNaN()
        context.assertNextNaN()

        // From third value onwards, should return n-2 previous values
        context.assertNext(5.0)   // 3rd bar, returns 1st value
        context.assertNext(10.0)  // 4th bar, returns 2nd value
        context.assertNext(15.0)  // 5th bar, returns 3rd value
        context.assertNext(20.0)  // 6th bar, returns 4th value
        context.assertNext(25.0)  // 7th bar, returns 5th value
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle negative values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(-10.0, -5.0, 0.0, 5.0, 10.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().previous(2))

        context.assertNextNaN()
        context.assertNextNaN()
        context.assertNext(-10.0)
        context.assertNext(-5.0)
        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle decimal values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.5, 2.75, 3.25, 4.125)
            .withIndicator(Indicators.extended(numFactory).closePrice().previous(1))

        context.assertNextNaN()
        context.assertNext(1.5)
        context.assertNext(2.75)
        context.assertNext(3.25)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should test lag property`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()

        val previous1 = PreviousNumericValueIndicator(numFactory, closePrice, 1)
        assertThat(previous1.lag).isEqualTo(1)

        val previous5 = PreviousNumericValueIndicator(numFactory, closePrice, 5)
        assertThat(previous5.lag).isEqualTo(5)

        val previous10 = PreviousNumericValueIndicator(numFactory, closePrice, 10)
        assertThat(previous10.lag).isEqualTo(10)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should test stability property`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(Indicators.extended(numFactory).closePrice())

        val closePrice = context.firstNumericIndicator!!
        val previous2 = PreviousNumericValueIndicator(numFactory, closePrice, 2)
        context.withIndicator(previous2)

        // Should not be stable initially
        assertThat(previous2.isStable).isFalse()

        context.advance() // 1st bar
        assertThat(previous2.isStable).isFalse()

        context.advance() // 2nd bar
        assertThat(previous2.isStable).isFalse()

        context.advance() // 3rd bar - should become stable (has value, not NaN)
        assertThat(previous2.isStable).isTrue()

        // Should remain stable
        context.advance() // 4th bar
        assertThat(previous2.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with different source indicators`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        // Create market events with different OHLC values
        listOf(
            context.withCandlePrices(10.0) // Will create events, but we'll override manually
        )

        // We'll test with SMA indicator as source
        context.withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0)
        val closePrice = Indicators.extended(numFactory).closePrice()
        val sma3 = closePrice.sma(3)
        val previousSma = PreviousNumericValueIndicator(numFactory, sma3, 1)

        context.withIndicator(sma3, "sma")
        context.withIndicator(previousSma, "previous_sma")

        // Skip until SMA is stable
        context.fastForward(3)

        // Get current SMA value
        val currentSma = sma3.value

        context.advance()

        // Previous SMA should equal the SMA from the previous bar
        assertNumEquals(currentSma, previousSma.value)
    }

    @Test
    fun `should throw exception for invalid n parameter`() {
        val numFactory = org.ta4j.core.num.NumFactoryProvider.defaultNumFactory
        val closePrice = Indicators.extended(numFactory).closePrice()

        assertThatThrownBy {
            PreviousNumericValueIndicator(numFactory, closePrice, 0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("n must be positive number")

        assertThatThrownBy {
            PreviousNumericValueIndicator(numFactory, closePrice, -1)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("n must be positive number")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct string representation`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val previous5 = PreviousNumericValueIndicator(numFactory, closePrice, 5)

        val stringRep = previous5.toString()
        assertThat(stringRep).contains("PREV")
        assertThat(stringRep).contains("5")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single data point`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(42.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().previous(1))

        // Single point with n=1 should return NaN
        context.assertNextNaN()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should chain multiple previous indicators`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 30.0, 40.0, 50.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val previous1 = closePrice.previous(1)
        val previous2OfPrevious1 = previous1.previous(1) // Should get value from 2 bars ago

        context.withIndicator(previous2OfPrevious1)

        context.assertNextNaN()
        context.assertNextNaN()

        context.assertNext(10.0)

        context.assertNext(20.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle constant values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(5.0, 5.0, 5.0, 5.0, 5.0)
            .withIndicator(Indicators.extended(numFactory).closePrice().previous(2))

        context.assertNextNaN()
        context.assertNextNaN()

        // All subsequent values should be 5.0
        repeat(3) {
            context.assertNext(5.0)
        }
    }
}
