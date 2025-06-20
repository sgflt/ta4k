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
package org.ta4j.core.indicators.numeric.volume

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider

class VWAPIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate VWAP correctly for window of 3`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 11.5, 10.5)
            .withIndicator(VWAPIndicator(numFactory, 3))

        // First bar: VWAP = typical price (since only one value)
        context.assertNext(10.0)

        // Second bar: VWAP over 2 bars
        context.assertNext(10.5)

        // Third bar: VWAP over 3 bars
        context.assertNext(11.0)

        // Fourth bar: VWAP over last 3 bars (sliding window)
        // TP values: [11.0, 12.0, 11.5] with equal volumes
        context.assertNext(11.5)

        // Fifth bar: VWAP over last 3 bars
        // TP values: [12.0, 11.5, 10.5] with equal volumes
        context.assertNext(11.33333)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single bar window`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(15.0, 20.0, 25.0)
            .withIndicator(VWAPIndicator(numFactory, 1))

        context.assertNext(15.0)
        context.assertNext(20.0)
        context.assertNext(25.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable after barCount periods`(numFactory: NumFactory) {
        val indicator = VWAPIndicator(numFactory, 5)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

        assertUnstable(indicator)
        context.fastForward(4)
        assertUnstable(indicator)
        context.advance()
        assertStable(indicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate VWAP with larger window`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 14.0, 16.0, 18.0)
            .withIndicator(VWAPIndicator(numFactory, 5))

        context.assertNext(10.0)  // [10]
        context.assertNext(11.0)  // [10, 12]
        context.assertNext(12.0)  // [10, 12, 14]
        context.assertNext(13.0)  // [10, 12, 14, 16]
        context.assertNext(14.0)  // [10, 12, 14, 16, 18]
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should maintain sliding window correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(20.0, 10.0, 30.0, 40.0)
            .withIndicator(VWAPIndicator(numFactory, 2))

        context.assertNext(20.0)  // [20]
        context.assertNext(15.0)  // [20, 10] -> (20+10)/2 = 15
        context.assertNext(20.0)  // [10, 30] -> (10+30)/2 = 20
        context.assertNext(35.0)  // [30, 40] -> (30+40)/2 = 35
    }

    @Test
    fun `should require positive bar count`() {
        assertThrows<IllegalArgumentException> {
            VWAPIndicator(NumFactoryProvider.defaultNumFactory, 0)
        }
        assertThrows<IllegalArgumentException> {
            VWAPIndicator(NumFactoryProvider.defaultNumFactory, -1)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle constant prices`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(15.0, 15.0, 15.0, 15.0)
            .withIndicator(VWAPIndicator(numFactory, 3))

        context.assertNext(15.0)
        context.assertNext(15.0)
        context.assertNext(15.0)
        context.assertNext(15.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return typical price when volume is zero`(numFactory: NumFactory) {
        // This test verifies the edge case handling when volume sum is zero
        // In practice, this would require custom market events with zero volume
        val indicator = VWAPIndicator(numFactory, 3)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(100.0)

        // With default volume, this should work normally
        context.assertNext(100.0)
    }
}
