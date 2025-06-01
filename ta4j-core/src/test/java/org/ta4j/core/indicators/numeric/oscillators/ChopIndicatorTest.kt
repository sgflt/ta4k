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
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertStable
import org.ta4j.core.TestUtils.assertUnstable
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory

class ChopIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate choppiness index correctly for trending market`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(ChopIndicator(numFactory, timeFrame = 3, scaleTo = 100))
            .withCandlePrices(
                10.0, 11.0, 12.0, 13.0, 14.0, 15.0
            )

        context.fastForwardUntilStable()

        // Once stable, should show low values for trending market
        context.advance()
        val trendingValue = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(trendingValue).isLessThan(50.0) // Trending markets have lower CHOP values

        context.advance()
        val nextTrendingValue = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(nextTrendingValue).isLessThan(50.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate choppiness index correctly for sideways market`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(ChopIndicator(numFactory, timeFrame = 3, scaleTo = 100))
            .withCandlePrices(
                10.0, 10.5, 10.0, 10.5, 10.0, 10.5
            )

        // Skip unstable period
        context.fastForward(3)

        // Sideways markets should have higher CHOP values
        val sidewaysValue = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(sidewaysValue).isGreaterThan(50.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero range correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(ChopIndicator(numFactory, timeFrame = 3, scaleTo = 100))
            .withCandlePrices(
                10.0, 10.0, 10.0, 10.0 // All same prices = zero range
            )

        // Should handle zero range without errors
        context.fastForward(3)
        context.assertCurrent(0.0) // Should return 0 when range is zero
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be unstable until enough bars are processed`(numFactory: NumFactory) {
        val indicator = ChopIndicator(numFactory, timeFrame = 5, scaleTo = 100)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)
            .withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

        repeat(5) {
            assertUnstable(indicator)
            context.advance()
        }

        assertStable(indicator)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag value`(numFactory: NumFactory) {
        val timeFrame = 14
        val indicator = ChopIndicator(numFactory, timeFrame, scaleTo = 100)

        assertThat(indicator.lag).isEqualTo(timeFrame)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate with different scaling factors`(numFactory: NumFactory) {
        val context100 = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(ChopIndicator(numFactory, timeFrame = 3, scaleTo = 100))
            .withCandlePrices(10.0, 11.0, 12.0, 13.0)

        val context1 = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(ChopIndicator(numFactory, timeFrame = 3, scaleTo = 1))
            .withCandlePrices(10.0, 11.0, 12.0, 13.0)

        // Skip to stable period
        context100.fastForward(3)
        context1.fastForward(3)

        val value100 = context100.firstNumericIndicator!!.value.doubleValue()
        val value1 = context1.firstNumericIndicator!!.value.doubleValue()

        // Values should be proportional to scaling factor
        assertThat(value100).isCloseTo(value1 * 100, org.assertj.core.data.Offset.offset(0.01))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should validate manual calculation for known values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(ChopIndicator(numFactory, timeFrame = 3, scaleTo = 100))
            .withMarketEvents(
                MockMarketEventBuilder()
                    .candle().highPrice(12.0).lowPrice(10.0).closePrice(11.0).add()
                    .candle().highPrice(13.0).lowPrice(11.0).closePrice(12.0).add()
                    .candle().highPrice(14.0).lowPrice(12.0).closePrice(13.0).add()
                    .candle().highPrice(15.0).lowPrice(13.0).closePrice(14.0).add()
                    .build()
            )

        // Skip to stable
        context.fastForwardUntilStable()
        // Manual calculation verification for last 3 bars (candles 2,3,4):
        // MaxHi(3) = max(13,14,15) = 15
        // MinLo(3) = min(11,12,13) = 11
        // Range = 15 - 11 = 4
        // ATR(1) for each bar = 2 (True Range for each consecutive bar)
        // ATR Sum = 2 + 2 + 2 = 6
        // CHOP = 100 * log10(6/4) / log10(3) = 100 * log10(1.5) / log10(3) ≈ 36.907
        context.assertCurrent(36.9070)
    }

    @Test
    fun `should throw exception for invalid timeframe`() {
        assertThatIllegalArgumentException()
            .isThrownBy { ChopIndicator(DoubleNumFactory, timeFrame = 0) }
            .withMessage("n must be positive number, but was: 0")

        assertThatIllegalArgumentException()
            .isThrownBy { ChopIndicator(DoubleNumFactory, timeFrame = -1) }
            .withMessage("n must be positive number, but was: -1")
    }

    @Test
    fun `should throw exception for invalid scale factor`() {
        assertThatIllegalArgumentException()
            .isThrownBy { ChopIndicator(DoubleNumFactory, timeFrame = 14, scaleTo = 0) }
            .withMessage("Scale factor must be positive")

        assertThatIllegalArgumentException()
            .isThrownBy { ChopIndicator(DoubleNumFactory, timeFrame = 14, scaleTo = -1) }
            .withMessage("Scale factor must be positive")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have meaningful toString representation`(numFactory: NumFactory) {
        val indicator = ChopIndicator(numFactory, timeFrame = 14, scaleTo = 100)

        assertThat(indicator.toString()).contains("CHOP(14, 100)")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with default scale factor`(numFactory: NumFactory) {
        val indicator = ChopIndicator(numFactory, timeFrame = 14) // Uses default scaleTo = 100

        assertThat(indicator.toString()).contains("CHOP(14, 100)")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle large price movements`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(ChopIndicator(numFactory, timeFrame = 3, scaleTo = 100))
            .withCandlePrices(
                100.0, 200.0, 50.0, 300.0, 25.0 // Large volatility
            )

        context.fastForwardUntilStable()

        // Should handle large movements without errors
        val chopValue = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(chopValue).isBetween(0.0, 100.0)
    }
}
