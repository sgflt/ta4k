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
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.numeric.pivotpoints

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.num.NumFactory

class FibonacciReversalIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate fibonacci resistance levels correctly`(numFactory: NumFactory) {
        val events = org.ta4j.core.mocks.MockMarketEventBuilder()
            .candle().openPrice(10.0).highPrice(12.0).lowPrice(8.0).closePrice(10.0).add()
            .candle().openPrice(12.0).highPrice(15.0).lowPrice(9.0).closePrice(12.0).add()
            .candle().openPrice(11.0).highPrice(14.0).lowPrice(10.0).closePrice(13.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)

        val pivotIndicator = PivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val fibResistance = FibonacciReversalIndicator(
            pivotIndicator,
            FibonacciFactor.FACTOR_2, // 0.618
            FibReversalType.RESISTANCE
        )

        context.withIndicator(fibResistance)

        // First value should be NaN (no previous data)
        context.assertNextNaN()

        // Second value: Pivot(10) + 0.618 * (12-8) = 10 + 0.618 * 4 = 10 + 2.472 = 12.472
        context.assertNext(12.472)

        // Third value: Pivot(12) + 0.618 * (15-9) = 12 + 0.618 * 6 = 12 + 3.708 = 15.708
        context.assertNext(15.708)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate fibonacci support levels correctly`(numFactory: NumFactory) {
        val events = org.ta4j.core.mocks.MockMarketEventBuilder()
            .candle().openPrice(10.0).highPrice(12.0).lowPrice(8.0).closePrice(10.0).add()
            .candle().openPrice(12.0).highPrice(15.0).lowPrice(9.0).closePrice(12.0).add()
            .candle().openPrice(11.0).highPrice(14.0).lowPrice(10.0).closePrice(13.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)

        val pivotIndicator = PivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val fibSupport = FibonacciReversalIndicator(
            pivotIndicator,
            0.382, // Using double constructor
            FibReversalType.SUPPORT
        )

        context.withIndicator(fibSupport)

        // First value should be NaN
        context.assertNextNaN()

        // Second value: Pivot(10) - 0.382 * (12-8) = 10 - 0.382 * 4 = 10 - 1.528 = 8.472
        context.assertNext(8.472)

        // Third value: Pivot(12) - 0.382 * (15-9) = 12 - 0.382 * 6 = 12 - 2.292 = 9.708
        context.assertNext(9.708)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle different fibonacci factors`(numFactory: NumFactory) {
        val events = org.ta4j.core.mocks.MockMarketEventBuilder()
            .candle().openPrice(10.0).highPrice(20.0).lowPrice(10.0).closePrice(15.0).add()
            .candle().openPrice(15.0).highPrice(25.0).lowPrice(15.0).closePrice(20.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)

        val pivotIndicator = PivotPointIndicator(numFactory, TimeLevel.BARBASED)

        // Test FACTOR_1 (0.382)
        val fib382 = FibonacciReversalIndicator(
            pivotIndicator,
            FibonacciFactor.FACTOR_1,
            FibReversalType.RESISTANCE
        )

        // Test FACTOR_3 (1.0)
        val fib100 = FibonacciReversalIndicator(
            pivotIndicator,
            FibonacciFactor.FACTOR_3,
            FibReversalType.RESISTANCE
        )

        context.withIndicator(fib382, "fib382")
            .withIndicator(fib100, "fib100")

        context.fastForwardUntilStable()

        // Day 1 Pivot = (20+10+15)/3 = 15, Range = 20-10 = 10
        // fib382: 15 + 0.382 * 10 = 15 + 3.82 = 18.82
        // fib100: 15 + 1.0 * 10 = 15 + 10 = 25
        assertNumEquals(18.82, fib382.value)
        assertNumEquals(25.0, fib100.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable when pivot point is stable`(numFactory: NumFactory) {
        val events = org.ta4j.core.mocks.MockMarketEventBuilder()
            .candle().openPrice(10.0).highPrice(12.0).lowPrice(8.0).closePrice(10.0).add()
            .candle().openPrice(12.0).highPrice(15.0).lowPrice(9.0).closePrice(12.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)

        val pivotIndicator = PivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val fibIndicator = FibonacciReversalIndicator(
            pivotIndicator,
            FibonacciFactor.FACTOR_2,
            FibReversalType.RESISTANCE
        )

        context.withIndicator(fibIndicator)

        // Should be unstable initially
        context.assertIsUnStable()
            .fastForward(1)

        // Should be unstable after first bar (no previous data)
        context.assertIsUnStable()
            .fastForward(1)

            // Should be stable after second bar (has previous data)
            .assertIsStable()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return NaN when pivot point is NaN`(numFactory: NumFactory) {
        val events = org.ta4j.core.mocks.MockMarketEventBuilder()
            .candle().openPrice(10.0).highPrice(12.0).lowPrice(8.0).closePrice(10.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)

        val pivotIndicator = PivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val fibIndicator = FibonacciReversalIndicator(
            pivotIndicator,
            0.618,
            FibReversalType.RESISTANCE
        )

        context.withIndicator(fibIndicator)

        // First value should be NaN when no previous data
        context.assertNextNaN()
        assertThat(fibIndicator.value.isNaN).isTrue()
    }
}
