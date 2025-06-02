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
package org.ta4j.core.indicators.numeric.oscillators

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory

class RAVIIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate RAVI correctly with uptrend data`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0)
            .withIndicator(RAVIIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 7))

        // Fast forward to when long SMA (7 periods) becomes stable
        context.fastForwardUntilStable()

        // At bar 7: prices [10,11,12,13,14,15,16]
        // Short SMA (3) = (14+15+16)/3 = 15.0
        // Long SMA (7) = (10+11+12+13+14+15+16)/7 = 13.0
        // RAVI = (15.0 - 13.0) / 13.0 * 100 = 2/13 * 100 ≈ 15.38461
        context.assertCurrent(15.38461)

        // At bar 8: prices [11,12,13,14,15,16,17]
        // Short SMA (3) = (15+16+17)/3 = 16.0
        // Long SMA (7) = (11+12+13+14+15+16+17)/7 = 14.0
        // RAVI = (16.0 - 14.0) / 14.0 * 100 = 2/14 * 100 ≈ 14.286
        context.assertNext(14.2857)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate negative RAVI in downtrend`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(20.0, 19.0, 18.0, 17.0, 16.0, 15.0, 14.0, 13.0, 12.0, 11.0, 10.0)
            .withIndicator(RAVIIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 7))

        context.fastForward(6)

        // In declining market, short SMA should be below long SMA, resulting in negative RAVI
        context.advance()
        assertThat(context.firstNumericIndicator!!.value.isNegative).isTrue()

        // Continue advancing to verify trend
        context.advance()
        assertThat(context.firstNumericIndicator!!.value.isNegative).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero long SMA gracefully`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0)
            .withIndicator(RAVIIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 7))

        context.fastForwardUntilStable() // Make both SMAs stable
        context.assertCurrent(0.0) // Should return 0 when long SMA is zero
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return zero when SMAs are equal`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0)
            .withIndicator(RAVIIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 7))

        context.fastForwardUntilStable() // Make both SMAs stable
        // When all prices are the same, both SMAs should be equal, resulting in RAVI = 0
        context.assertCurrent(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag equal to long SMA period`(numFactory: NumFactory) {
        val indicator = RAVIIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 7, 65)
        assertThat(indicator.lag).isEqualTo(65)

        val indicator2 = RAVIIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 20)
        assertThat(indicator2.lag).isEqualTo(20)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable when both SMAs are stable`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0)
            .withIndicator(RAVIIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 7))

        // Before long SMA is stable (less than 7 bars)
        context.fastForward(6)
        assertThat(context.firstNumericIndicator!!.isStable).isFalse()

        // After long SMA becomes stable (7 bars processed)
        context.advance()
        context.assertIsStable()
    }

    @Test
    fun `should use default parameters when not specified`() {
        val indicator = RAVIIndicator(DoubleNumFactory, Indicators.extended(DoubleNumFactory).closePrice())

        assertThat(indicator.lag).isEqualTo(65) // Default long SMA period
        assertThat(indicator.toString()).contains("RAVI(7, 65)")
    }

    @Test
    fun `should validate constructor parameters`() {
        val closePrice = Indicators.extended(DoubleNumFactory).closePrice()

        // Should throw for non-positive periods
        assertThrows<IllegalArgumentException> {
            RAVIIndicator(DoubleNumFactory, closePrice, 0, 65)
        }

        assertThrows<IllegalArgumentException> {
            RAVIIndicator(DoubleNumFactory, closePrice, 7, 0)
        }

        assertThrows<IllegalArgumentException> {
            RAVIIndicator(DoubleNumFactory, closePrice, -1, 65)
        }

        // Should throw when short period >= long period
        assertThrows<IllegalArgumentException> {
            RAVIIndicator(DoubleNumFactory, closePrice, 65, 7)
        }

        assertThrows<IllegalArgumentException> {
            RAVIIndicator(DoubleNumFactory, closePrice, 7, 7)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should maintain trend direction without absolute value`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 115.0, 120.0, 125.0, 130.0, 135.0, 140.0)
            .withIndicator(RAVIIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 7))

        context.fastForwardUntilStable()

        // In uptrend, RAVI should be positive (short SMA > long SMA)
        assertThat(context.firstNumericIndicator!!.value.isPositive).isTrue()

        context.advance()
        // Should continue to be positive in strong uptrend
        assertThat(context.firstNumericIndicator!!.value.isPositive).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle volatile market conditions`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 90.0, 110.0, 95.0, 105.0, 88.0, 112.0, 92.0, 108.0, 96.0)
            .withIndicator(RAVIIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 3, 7))

        context.fastForwardUntilStable()

        // In volatile market, RAVI should fluctuate around zero
        val firstValue = context.firstNumericIndicator!!.value
        context.advance()
        val secondValue = context.firstNumericIndicator!!.value

        // Values should be different due to volatility
        assertThat(firstValue).isNotEqualTo(secondValue)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with different time periods`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 14.0, 16.0, 18.0, 20.0)
            .withIndicator(RAVIIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 2, 4))

        // With shorter periods, should be stable sooner
        context.fastForward(4) // 4 bars for long SMA to be stable
        context.assertIsStable()

        // RAVI should be positive in this uptrend
        assertThat(context.firstNumericIndicator!!.value.isPositive).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should produce string representation`(numFactory: NumFactory) {
        val indicator = RAVIIndicator(
            numFactory,
            Indicators.extended(numFactory).closePrice(),
            5,
            20
        )

        assertThat(indicator.toString()).contains("RAVI(5, 20)")
    }
}
