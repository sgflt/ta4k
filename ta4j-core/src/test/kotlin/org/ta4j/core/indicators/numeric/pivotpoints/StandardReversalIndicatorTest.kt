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
import org.ta4j.core.num.NumFactory

class StandardReversalIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate resistance levels correctly`(numFactory: NumFactory) {
        val events = org.ta4j.core.mocks.MockMarketEventBuilder()
            .candle().openPrice(10.0).highPrice(20.0).lowPrice(10.0).closePrice(15.0).add()
            .candle().openPrice(15.0).highPrice(25.0).lowPrice(15.0).closePrice(20.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)

        val pivotIndicator = PivotPointIndicator(numFactory, TimeLevel.BARBASED)

        // Test all resistance levels
        val r1 = StandardReversalIndicator(pivotIndicator, PivotLevel.RESISTANCE_1)
        val r2 = StandardReversalIndicator(pivotIndicator, PivotLevel.RESISTANCE_2)
        val r3 = StandardReversalIndicator(pivotIndicator, PivotLevel.RESISTANCE_3)

        context.withIndicator(r1, "r1")
            .withIndicator(r2, "r2")
            .withIndicator(r3, "r3")

        context.assertNextNaN() // First bar

        // Day 1: Pivot = (20+10+15)/3 = 15, High = 20, Low = 10, Range = 10
        // R1 = 2 * Pivot - Low = 2 * 15 - 10 = 20
        // R2 = Pivot + (High - Low) = 15 + 10 = 25
        // R3 = High + 2 * (Pivot - Low) = 20 + 2 * (15 - 10) = 20 + 10 = 30
        context.onIndicator("r1").assertNext(20.0)
        context.onIndicator("r2").assertCurrent(25.0)
        context.onIndicator("r3").assertCurrent(30.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate support levels correctly`(numFactory: NumFactory) {
        val events = org.ta4j.core.mocks.MockMarketEventBuilder()
            .candle().openPrice(10.0).highPrice(20.0).lowPrice(10.0).closePrice(15.0).add()
            .candle().openPrice(15.0).highPrice(25.0).lowPrice(15.0).closePrice(20.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)

        val pivotIndicator = PivotPointIndicator(numFactory, TimeLevel.BARBASED)

        // Test all support levels
        val s1 = StandardReversalIndicator(pivotIndicator, PivotLevel.SUPPORT_1)
        val s2 = StandardReversalIndicator(pivotIndicator, PivotLevel.SUPPORT_2)
        val s3 = StandardReversalIndicator(pivotIndicator, PivotLevel.SUPPORT_3)

        context.withIndicator(s1, "s1")
            .withIndicator(s2, "s2")
            .withIndicator(s3, "s3")

        context.assertNextNaN() // First bar

        // Day 1: Pivot = 15, High = 20, Low = 10
        // S1 = 2 * Pivot - High = 2 * 15 - 20 = 10
        // S2 = Pivot - (High - Low) = 15 - 10 = 5
        // S3 = Low - 2 * (High - Pivot) = 10 - 2 * (20 - 15) = 10 - 10 = 0
        context.onIndicator("s1").assertNext(10.0)
        context.onIndicator("s2").assertCurrent(5.0)
        context.onIndicator("s3").assertCurrent(0.0)
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
        val standardReversal = StandardReversalIndicator(pivotIndicator, PivotLevel.RESISTANCE_1)

        context.withIndicator(standardReversal)

        // Should be unstable initially
        context.assertIsUnStable().fastForward(1)

        // Should be unstable after first bar (no previous data)
        context.assertIsUnStable().fastForward(1)

        // Should be stable after second bar (has previous data)
        context.assertIsStable()
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
        val standardReversal = StandardReversalIndicator(pivotIndicator, PivotLevel.RESISTANCE_1)

        context.withIndicator(standardReversal)

        // First value should be NaN when no previous data
        context.assertNextNaN()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle various market conditions`(numFactory: NumFactory) {
        val events = org.ta4j.core.mocks.MockMarketEventBuilder()
            // Volatile day
            .candle().openPrice(50.0).highPrice(60.0).lowPrice(40.0).closePrice(55.0).add()
            // Calm day
            .candle().openPrice(55.0).highPrice(56.0).lowPrice(54.0).closePrice(55.5).add()
            // Trending up day
            .candle().openPrice(55.5).highPrice(65.0).lowPrice(55.0).closePrice(64.0).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)

        val pivotIndicator = PivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val r1 = StandardReversalIndicator(pivotIndicator, PivotLevel.RESISTANCE_1)
        val s1 = StandardReversalIndicator(pivotIndicator, PivotLevel.SUPPORT_1)

        context.withIndicator(r1, "r1")
            .withIndicator(s1, "s1")

        context.assertNextNaN() // First bar
        context.fastForward(1)

        // Day 1: Pivot = (60+40+55)/3 = 51.67, High = 60, Low = 40
        // R1 = 2 * 51.67 - 40 = 63.34
        // S1 = 2 * 51.67 - 60 = 43.34
        context.onIndicator("r1").assertCurrent(63.333333333333336)
        context.onIndicator("s1").assertCurrent(43.333333333333336)

        context.fastForward(1)

        // Day 2: Pivot = (56+54+55.5)/3 = 55.17, High = 56, Low = 54  
        // R1 = 2 * 55.17 - 54 = 56.34
        // S1 = 2 * 55.17 - 56 = 54.34
        context.onIndicator("r1").assertCurrent(56.333333333333336)
        context.onIndicator("s1").assertCurrent(54.333333333333336)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag property`(numFactory: NumFactory) {
        val pivotIndicator = PivotPointIndicator(numFactory, TimeLevel.BARBASED)
        val standardReversal = StandardReversalIndicator(pivotIndicator, PivotLevel.RESISTANCE_1)

        // Should have lag of pivot + 1
        assertThat(standardReversal.lag).isEqualTo(pivotIndicator.lag + 1)
    }
}
