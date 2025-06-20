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
import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.helpers.FixedBooleanIndicator
import org.ta4j.core.num.NumFactory

class PreviousBooleanValueIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return previous boolean value with n=1`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)

        // Create a boolean indicator based on price comparison
        val closePrice = Indicators.extended(numFactory).closePrice()
        val isHigh = object : BooleanIndicator() {
            override fun updateState(bar: Bar) {
                closePrice.onBar(bar)
                value = closePrice.isGreaterThan(2.5)
            }

            override val lag = closePrice.lag
            override val isStable = closePrice.isStable

        }
        val previousValue = PreviousBooleanValueIndicator(isHigh, 1)

        context.withIndicator(previousValue)

        context.assertNextFalse()

        // value should be false (previous was false: 1.0 > 2.5 = false)
        context.assertNextFalse()

        // value should be false (previous was false: 2.0 > 2.5 = false)
        context.assertNextFalse()

        // value should be true (previous was true: 3.0 > 2.5 = true)
        context.assertNextTrue()

        // value should be true (previous was true: 4.0 > 2.5 = true)
        context.assertNextTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return previous boolean value with n=2`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(true, false, true, true, false, true)
        val previousValue = PreviousBooleanValueIndicator(fixedIndicator, 2)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
            .withIndicator(previousValue)

        // First two values should be false (not enough previous values)
        context.assertNextFalse()
        context.assertNextFalse()

        // Third value should be true (value from 2 bars ago: true)
        context.assertNextTrue()

        // Fourth value should be false (value from 2 bars ago: false)
        context.assertNextFalse()

        // Fifth value should be true (value from 2 bars ago: true)
        context.assertNextTrue()

        // Sixth value should be true (value from 2 bars ago: true)
        context.assertNextTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle alternating boolean pattern`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(true, false, true, false, true, false)
        val previousValue = PreviousBooleanValueIndicator(fixedIndicator, 1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
            .withIndicator(previousValue)

        // First should be false (no previous)
        context.assertNextFalse()

        // Then alternating pattern shifted by one
        context.assertNextTrue()  // previous: true
        context.assertNextFalse() // previous: false
        context.assertNextTrue()  // previous: true
        context.assertNextFalse() // previous: false
        context.assertNextTrue()  // previous: true
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle all true values`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(true, true, true, true, true)
        val previousValue = PreviousBooleanValueIndicator(fixedIndicator, 2)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(previousValue)

        // First two should be false (not enough data)
        context.assertNextFalse()
        context.assertNextFalse()

        // Rest should be true
        repeat(3) {
            context.assertNextTrue()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle all false values`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(false, false, false, false, false)
        val previousValue = PreviousBooleanValueIndicator(fixedIndicator, 1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(previousValue)

        // All should be false
        repeat(5) {
            context.assertNextFalse()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle large n value`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(true, true, false, true, false)
        val previousValue = PreviousBooleanValueIndicator(fixedIndicator, 10)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(previousValue)

        // All should be false since n=10 > available data
        repeat(5) {
            context.assertNextFalse()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should test lag property`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(true, false, true)

        val previous1 = PreviousBooleanValueIndicator(fixedIndicator, 1)
        assertThat(previous1.lag).isEqualTo(1)

        val previous5 = PreviousBooleanValueIndicator(fixedIndicator, 5)
        assertThat(previous5.lag).isEqualTo(5)

        val previous10 = PreviousBooleanValueIndicator(fixedIndicator, 10)
        assertThat(previous10.lag).isEqualTo(10)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should test stability property`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(true, false, true, false, true)
        val previousValue = PreviousBooleanValueIndicator(fixedIndicator, 2)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(previousValue)

        // Should not be stable initially
        assertThat(previousValue.isStable).isFalse()

        context.advance() // 1st bar
        assertThat(previousValue.isStable).isFalse()

        context.advance() // 2nd bar
        assertThat(previousValue.isStable).isFalse()

        // Should remain stable
        while (context.advance()) {
            assertThat(previousValue.isStable).isTrue()
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with cross indicator as source`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 15.0, 12.0, 18.0, 16.0, 20.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val sma3 = closePrice.sma(3)
        val crossIndicator = closePrice.crossedOver(sma3)
        val previousCross = PreviousBooleanValueIndicator(crossIndicator, 1)

        context.withIndicator(crossIndicator, "cross")
        context.withIndicator(previousCross, "previous_cross")

        // Skip until indicators are stable
        context.fastForwardUntilStable()

        val currentCross = crossIndicator.value
        context.advance()

        // Previous cross should equal the cross from the previous bar
        assertThat(previousCross.value).isEqualTo(currentCross)
    }

    @Test
    fun `should throw exception for invalid n parameter`() {
        val fixedIndicator = FixedBooleanIndicator(true, false, true)

        assertThatThrownBy {
            PreviousBooleanValueIndicator(fixedIndicator, 0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("n must be positive number")

        assertThatThrownBy {
            PreviousBooleanValueIndicator(fixedIndicator, -1)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("n must be positive number")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should convert to rule for strategy usage`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(true, false, true, false)
        val previousValue = PreviousBooleanValueIndicator(fixedIndicator, 1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0)
            .withIndicator(previousValue)

        // Convert to rule for use in strategies
        val rule = previousValue.toRule()

        context.advance() // First: false (no previous)
        assertThat(rule.isSatisfied).isFalse()

        context.advance() // Second: true (previous was true)
        assertThat(rule.isSatisfied).isTrue()

        context.advance() // Third: false (previous was false)
        assertThat(rule.isSatisfied).isFalse()

        context.advance() // Fourth: true (previous was true)
        assertThat(rule.isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single data point`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(true)
        val previousValue = PreviousBooleanValueIndicator(fixedIndicator, 1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(42.0)
            .withIndicator(previousValue)

        // Single point with n=1 should return false (no previous)
        context.assertNextFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should chain multiple previous boolean indicators`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(true, false, true, false, true)
        val previous1 = PreviousBooleanValueIndicator(fixedIndicator, 1)
        val previous2OfPrevious1 = PreviousBooleanValueIndicator(previous1, 1) // Should get value from 2 bars ago

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0)
            .withIndicator(previous2OfPrevious1)

        context.assertNextFalse()
        context.assertNextFalse()
        context.assertNextTrue()
        context.assertNextFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with mixed true false pattern with different lags`(numFactory: NumFactory) {
        val fixedIndicator = FixedBooleanIndicator(true, true, false, false, true, true, false)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)

        val previous3 = PreviousBooleanValueIndicator(fixedIndicator, 3)
        context.withIndicator(previous3)

        // First three should be false (not enough data)
        repeat(3) {
            context.assertNextFalse()
        }

        // Fourth should be true (3 bars ago: true)
        context.assertNextTrue()

        // Fifth should be true (3 bars ago: true)
        context.assertNextTrue()

        // Sixth should be false (3 bars ago: false)
        context.assertNextFalse()

        // Seventh should be false (3 bars ago: false)
        context.assertNextFalse()
    }
}
