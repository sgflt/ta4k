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
package org.ta4j.core.indicators.numeric.channels.donchian

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.num.NumFactory

class DonchianChannelMiddleIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate middle of Donchian Channel correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 8.0, 15.0, 9.0, 11.0, 13.0, 7.0, 14.0, 10.0)

        val indicator = DonchianChannelMiddleIndicator(numFactory, 3)
        context.withIndicator(indicator)

        // Fast forward until stable (first 3 bars)
        context.fastForwardUntilStable()

        // For period 3 with prices [10, 12, 8]:
        // Highest = 12, Lowest = 8, Middle = (12 + 8) / 2 = 10
        context.assertCurrent(10.0)

        // For period 3 with prices [12, 8, 15]:
        // Highest = 15, Lowest = 8, Middle = (15 + 8) / 2 = 11.5
        context.assertNext(11.5)

        // For period 3 with prices [8, 15, 9]:
        // Highest = 15, Lowest = 8, Middle = (15 + 8) / 2 = 11.5
        context.assertNext(11.5)

        // For period 3 with prices [15, 9, 11]:
        // Highest = 15, Lowest = 9, Middle = (15 + 9) / 2 = 12
        context.assertNext(12.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single period correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 95.0, 110.0, 85.0)

        val indicator = DonchianChannelMiddleIndicator(numFactory, 1)
        context.withIndicator(indicator)

        // With period 1, middle equals the price itself (since high=low=price)
        context.assertNext(100.0)
        context.assertNext(105.0)
        context.assertNext(95.0)
        context.assertNext(110.0)
        context.assertNext(85.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable only after sufficient data points`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 20.0, 15.0, 25.0, 30.0)

        val indicator = DonchianChannelMiddleIndicator(numFactory, 3)
        context.withIndicator(indicator)

        // Should not be stable for first 2 bars
        context.advance()
        assertUnstable(indicator)

        context.advance()
        assertUnstable(indicator)

        // Should be stable from 3rd bar onwards
        context.advance()
        assertStable(indicator)

        context.advance()
        assertStable(indicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag value`(numFactory: NumFactory) {
        val indicator5 = DonchianChannelMiddleIndicator(numFactory, 5)
        assertEquals(5, indicator5.lag)

        val indicator14 = DonchianChannelMiddleIndicator(numFactory, 14)
        assertEquals(14, indicator14.lag)

        val indicator20 = DonchianChannelMiddleIndicator(numFactory, 20)
        assertEquals(20, indicator20.lag)
    }

    @Test
    fun `should require positive bar count`() {
        val numFactory = org.ta4j.core.num.DoubleNumFactory

        assertThrows<IllegalArgumentException> {
            DonchianChannelMiddleIndicator(numFactory, 0)
        }

        assertThrows<NegativeArraySizeException> {
            DonchianChannelMiddleIndicator(numFactory, -1)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle identical prices correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 100.0, 100.0, 100.0)

        val indicator = DonchianChannelMiddleIndicator(numFactory, 2)
        context.withIndicator(indicator)

        context.advance()
        context.advance()

        // When all prices are identical, middle should equal that value
        assertNumEquals(100.0, indicator.value)

        context.assertNext(100.0)
        context.assertNext(100.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should produce correct string representation`(numFactory: NumFactory) {
        val indicator = DonchianChannelMiddleIndicator(numFactory, 14)

        // Should contain class name and bar count
        val stringRep = indicator.toString()
        assert(stringRep.contains("DCM"))
        assert(stringRep.contains("14"))
    }
}
