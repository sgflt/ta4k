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
package org.ta4j.core.indicators.numeric.oscilators

import kotlin.test.assertFailsWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class DPOIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle constant prices`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val dpo = DPOIndicator(numFactory, closePrice, 4)

        context.withIndicator(dpo)

        // Fast forward to stability
        context.fastForwardUntilStable()

        // With constant prices, DPO should be 0
        context.assertNext(0.0)
        context.assertNext(0.0)
        context.assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single value`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(50.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val dpo = DPOIndicator(numFactory, closePrice, 1)

        context.withIndicator(dpo)

        context.fastForward(1)
        assertUnstable(dpo) // Should still be unstable with just one value
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should verify lag calculation`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val dpo = DPOIndicator(numFactory, closePrice, 10)

        // Lag should be barCount + timeFrames = 10 + (10/2 + 1) = 10 + 6 = 16
        assertThat(dpo.lag).isEqualTo(16)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should verify lag calculation for different periods`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()

        val dpo5 = DPOIndicator(numFactory, closePrice, 5)
        assertThat(dpo5.lag).isEqualTo(5 + 3) // 5 + (5/2 + 1) = 8

        val dpo20 = DPOIndicator(numFactory, closePrice, 20)
        assertThat(dpo20.lag).isEqualTo(20 + 11) // 20 + (20/2 + 1) = 31
    }

    @Test
    fun `should throw exception for negative bar count`() {
        val numFactory = org.ta4j.core.num.DoubleNumFactory
        val closePrice = Indicators.extended(numFactory).closePrice()

        assertFailsWith<IllegalArgumentException> {
            DPOIndicator(numFactory, closePrice, -1)
        }
    }

    @Test
    fun `should throw exception for zero bar count`() {
        val numFactory = org.ta4j.core.num.DoubleNumFactory
        val closePrice = Indicators.extended(numFactory).closePrice()

        assertFailsWith<IllegalArgumentException> {
            DPOIndicator(numFactory, closePrice, 0)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should produce string representation`(numFactory: NumFactory) {
        val closePrice = Indicators.extended(numFactory).closePrice()
        val dpo = DPOIndicator(numFactory, closePrice, 14)

        val stringRepresentation = dpo.toString()
        assertThat(stringRepresentation).contains("DPO")
        assertThat(stringRepresentation).contains("14")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable only after sufficient bars`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val dpo = DPOIndicator(numFactory, closePrice, 5)

        context.withIndicator(dpo)

        // Should be unstable initially
        (1..7).forEach { i ->
            context.fastForward(1)
            assertUnstable(dpo)
        }

        // Should be stable after lag period
        context.fastForward(1)
        assertStable(dpo)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate DPO correctly against manual calculation`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 100.0, 200.0)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val dpo = DPOIndicator(numFactory, closePrice, 5)

        context.withIndicator(dpo)


        // Fast forward to stability
        context.fastForwardUntilStable() // Need 5 + 3 = 8 bars for stability

        // Next bar:
        // - Current price is 18.0 (bar 9)
        // - SMA(5) 3 bars ago was for bars 2-6: (11+12+13+14+15)/5 = 13.0
        // - DPO = 18.0 - 13.0 = 5.0
        context.assertNext(5.0)

        // Next bar:
        // - Current price is 100.0 (bar 10)
        // - SMA(5) 3 bars ago was for bars 2-6: (12+13+14+15+16)/5 = 14.0
        // - DPO = 100.0 - 14.0 = 86.0
        context.assertNext(86.0)

        // Next bar:
        // - Current price is 200.0 (bar 11)
        // - SMA(5) 3 bars ago was for bars 3-7: (13+14+15+16+17)/5 = 15.0
        // - DPO = 200.0 - 15.0 = 185.0
        context.assertNext(185.0)
    }
}
