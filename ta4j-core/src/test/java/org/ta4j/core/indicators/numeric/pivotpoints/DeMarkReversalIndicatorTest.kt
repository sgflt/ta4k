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
package org.ta4j.core.indicators.numeric.pivotpoints

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.num.NumFactory

class DeMarkReversalIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should return NaN when pivot point is not stable")
    fun `should return NaN when pivot point is not stable`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 8.0, 11.0)

        val pivotIndicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val resistanceIndicator = DeMarkReversalIndicator(pivotIndicator, DeMarkPivotLevel.RESISTANCE)
        val supportIndicator = DeMarkReversalIndicator(pivotIndicator, DeMarkPivotLevel.SUPPORT)

        context.withIndicator(resistanceIndicator, "resistance")
            .withIndicator(supportIndicator, "support")

        // First bar should return NaN since pivot is not stable yet
        context.assertNextNaN()
        context.onIndicator("support").assertCurrent(Double.NaN)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should calculate resistance correctly for bar-based timeframe")
    fun `should calculate resistance correctly for bar-based timeframe`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 8.0, 11.0, 13.0)

        val pivotIndicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val resistanceIndicator = DeMarkReversalIndicator(pivotIndicator, DeMarkPivotLevel.RESISTANCE)

        context.withIndicator(resistanceIndicator)

        // First bar: NaN (not stable)
        context.assertNextNaN()

        // Second bar: Calculate based on first bar
        // First bar: open=10, high=10, low=10, close=10
        // Since close == open: X = high + low + (2 × close) = 10 + 10 + 20 = 40
        // Pivot = 40 / 4 = 10
        // Resistance = (10 × 2) - 10 = 10 (previous low)
        context.assertNext(10.0)

        // Third bar: Calculate based on second bar
        // Second bar: open=12, high=12, low=12, close=12
        // Since close == open: X = 12 + 12 + 24 = 48
        // Pivot = 48 / 4 = 12
        // Resistance = (12 × 2) - 12 = 12
        context.assertNext(12.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should calculate support correctly for bar-based timeframe")
    fun `should calculate support correctly for bar-based timeframe`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 8.0, 11.0, 13.0)

        val pivotIndicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val supportIndicator = DeMarkReversalIndicator(pivotIndicator, DeMarkPivotLevel.SUPPORT)

        context.withIndicator(supportIndicator)

        // First bar: NaN (not stable)
        context.assertNextNaN()

        // Second bar: Calculate based on first bar
        // First bar: open=10, high=10, low=10, close=10
        // Pivot = 10 (as calculated above)
        // Support = (10 × 2) - 10 = 10 (previous high)
        context.assertNext(10.0)

        // Third bar: Calculate based on second bar
        // Second bar: pivot = 12
        // Support = (12 × 2) - 12 = 12
        context.assertNext(12.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should handle DeMark pivot formula variations correctly")
    fun `should handle DeMark pivot formula variations correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val pivotIndicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val resistanceIndicator = DeMarkReversalIndicator(pivotIndicator, DeMarkPivotLevel.RESISTANCE)
        val supportIndicator = DeMarkReversalIndicator(pivotIndicator, DeMarkPivotLevel.SUPPORT)

        context.withIndicator(resistanceIndicator, "resistance")
            .withIndicator(supportIndicator, "support")

        // Test case: close > open
        // Bar with open=8, high=12, low=6, close=10
        context.withMarketEvents(
            listOf(
                CandleReceived(
                    org.ta4j.core.indicators.TimeFrame.DAY,
                    java.time.Instant.ofEpochSecond(0),
                    java.time.Instant.ofEpochSecond(86400),
                    8.0, 12.0, 6.0, 10.0, 1000.0
                ),
                CandleReceived(
                    org.ta4j.core.indicators.TimeFrame.DAY,
                    java.time.Instant.ofEpochSecond(86400),
                    java.time.Instant.ofEpochSecond(172800),
                    9.0, 11.0, 7.0, 9.0, 1000.0
                )
            )
        )

        // First bar: NaN
        context.assertNextNaN()
        context.onIndicator("support").assertCurrent(Double.NaN)

        // Second bar:
        // First bar had close(10) > open(8), so: X = (2 × 12) + 6 + 10 = 40
        // Pivot = 40 / 4 = 10
        // Resistance = (10 × 2) - 6 = 14 (previous low = 6)
        // Support = (10 × 2) - 12 = 8 (previous high = 12)
        context.onIndicator("resistance").assertNext(14.0)
        context.onIndicator("support").assertCurrent(8.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should be stable when pivot indicator is stable")
    fun `should be stable when pivot indicator is stable`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0)

        val pivotIndicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val resistanceIndicator = DeMarkReversalIndicator(pivotIndicator, DeMarkPivotLevel.RESISTANCE)

        context.withIndicator(resistanceIndicator)

        // First bar: not stable
        context.advance()
        assertThat(resistanceIndicator.isStable).isFalse()

        // Second bar: should be stable
        context.advance()
        assertThat(resistanceIndicator.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should have same lag as pivot indicator")
    fun `should have same lag as pivot indicator`(numFactory: NumFactory) {
        val pivotIndicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val resistanceIndicator = DeMarkReversalIndicator(pivotIndicator, DeMarkPivotLevel.RESISTANCE)

        assertThat(resistanceIndicator.lag).isEqualTo(pivotIndicator.lag)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    @DisplayName("Should handle close less than open case")
    fun `should handle close less than open case`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)

        val pivotIndicator = DeMarkPivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val resistanceIndicator = DeMarkReversalIndicator(pivotIndicator, DeMarkPivotLevel.RESISTANCE)

        context.withIndicator(resistanceIndicator)

        // Create bars where close < open
        context.withMarketEvents(
            listOf(
                CandleReceived(
                    org.ta4j.core.indicators.TimeFrame.DAY,
                    java.time.Instant.ofEpochSecond(0),
                    java.time.Instant.ofEpochSecond(86400),
                    110.0, 115.0, 105.0, 108.0, 1000.0 // close < open
                ),
                CandleReceived(
                    org.ta4j.core.indicators.TimeFrame.DAY,
                    java.time.Instant.ofEpochSecond(86400),
                    java.time.Instant.ofEpochSecond(172800),
                    108.0, 112.0, 106.0, 110.0, 1000.0
                )
            )
        )

        // First bar: NaN
        context.assertNextNaN()

        // Second bar:
        // First bar had close(108) < open(110), so: X = 115 + (2 × 105) + 108 = 433
        // Pivot = 433 / 4 = 108.25
        // Resistance = (108.25 × 2) - 105 = 111.5
        context.assertNext(111.5)
    }
}
