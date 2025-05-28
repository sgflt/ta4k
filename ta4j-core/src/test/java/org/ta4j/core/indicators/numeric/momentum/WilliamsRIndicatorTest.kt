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

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.numeric.momentum.WilliamsRIndicator
import org.ta4j.core.num.NumFactory

class WilliamsRIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate Williams R correctly with known values`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(
                listOf(
                    // Bar 1: H=15, L=10, C=12
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(1),
                        Instant.ofEpochSecond(2),
                        11.0,
                        15.0,
                        10.0,
                        12.0,
                        1000.0
                    ),
                    // Bar 2: H=16, L=11, C=14
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(2),
                        Instant.ofEpochSecond(3),
                        12.0,
                        16.0,
                        11.0,
                        14.0,
                        1000.0
                    ),
                    // Bar 3: H=18, L=13, C=15
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(3),
                        Instant.ofEpochSecond(4),
                        14.0,
                        18.0,
                        13.0,
                        15.0,
                        1000.0
                    ),
                    // Bar 4: H=17, L=12, C=13
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(4),
                        Instant.ofEpochSecond(5),
                        15.0,
                        17.0,
                        12.0,
                        13.0,
                        1000.0
                    ),
                    // Bar 5: H=19, L=14, C=16
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(5),
                        Instant.ofEpochSecond(6),
                        13.0,
                        19.0,
                        14.0,
                        16.0,
                        1000.0
                    )
                )
            )

        val williamsR = WilliamsRIndicator(numFactory, 3)
        context.withIndicator(williamsR, "williamsR")

        // First bar: not stable yet
        context.assertNext(0.0)

        // Second bar: still not stable (need 3 bars)
        context.assertNext(0.0)

        // Third bar: now stable
        // Highest high over 3 bars = 18, Lowest low = 10, Close = 15
        // %R = (18 - 15) / (18 - 10) * -100 = 3/8 * -100 = -37.5
        context.assertNext(-37.5)

        // Fourth bar:
        // Highest high over last 3 bars (bars 2,3,4) = 18, Lowest low = 11, Close = 13
        // %R = (18 - 13) / (18 - 11) * -100 = 5/7 * -100 ≈ -71.43
        context.onIndicator("williamsR").assertNext(-71.4286)

        // Fifth bar:
        // Highest high over last 3 bars (bars 3,4,5) = 19, Lowest low = 12, Close = 16
        // %R = (19 - 16) / (19 - 12) * -100 = 3/7 * -100 ≈ -42.86
        context.onIndicator("williamsR").assertNext(-42.8571)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle extreme overbought condition`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(
                listOf(
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(1),
                        Instant.ofEpochSecond(2),
                        10.0,
                        20.0,
                        10.0,
                        15.0,
                        1000.0
                    ),
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(2),
                        Instant.ofEpochSecond(3),
                        15.0,
                        25.0,
                        15.0,
                        20.0,
                        1000.0
                    ),
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(3),
                        Instant.ofEpochSecond(4),
                        20.0,
                        30.0,
                        20.0,
                        30.0,
                        1000.0
                    ) // Close at highest high
                )
            )

        val williamsR = WilliamsRIndicator(numFactory, 3)
        context.withIndicator(williamsR)

        context.fastForward(3)
        // When close equals highest high: %R = (30 - 30) / (30 - 10) * -100 = 0
        assertNumEquals(0.0, williamsR.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle extreme oversold condition`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(
                listOf(
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(1),
                        Instant.ofEpochSecond(2),
                        15.0,
                        20.0,
                        10.0,
                        15.0,
                        1000.0
                    ),
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(2),
                        Instant.ofEpochSecond(3),
                        20.0,
                        25.0,
                        15.0,
                        20.0,
                        1000.0
                    ),
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(3),
                        Instant.ofEpochSecond(4),
                        20.0,
                        30.0,
                        10.0,
                        10.0,
                        1000.0
                    ) // Close at lowest low
                )
            )

        val williamsR = WilliamsRIndicator(numFactory, 3)
        context.withIndicator(williamsR)

        context.fastForward(3)
        // When close equals lowest low: %R = (30 - 10) / (30 - 10) * -100 = -100
        assertNumEquals(-100.0, williamsR.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero range scenario`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(
                listOf(
                    // All bars have same high and low (no range)
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(1),
                        Instant.ofEpochSecond(2),
                        15.0,
                        15.0,
                        15.0,
                        15.0,
                        1000.0
                    ),
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(2),
                        Instant.ofEpochSecond(3),
                        15.0,
                        15.0,
                        15.0,
                        15.0,
                        1000.0
                    ),
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(3),
                        Instant.ofEpochSecond(4),
                        15.0,
                        15.0,
                        15.0,
                        15.0,
                        1000.0
                    )
                )
            )

        val williamsR = WilliamsRIndicator(numFactory, 3)
        context.withIndicator(williamsR)

        context.fastForward(3)
        // When highest high equals lowest low, should return 0
        assertNumEquals(0.0, williamsR.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should not be stable until enough bars are processed`(numFactory: NumFactory) {
        val williamsR = WilliamsRIndicator(numFactory, 5)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0, 15.0)
            .withIndicator(williamsR)

        // Should not be stable for first 4 bars
        repeat(4) {
            context.advance()
            assertThat(williamsR.isStable).isFalse()
        }

        // Should be stable after 5th bar
        context.advance()
        assertThat(williamsR.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag value`(numFactory: NumFactory) {
        val barCount = 14
        val williamsR = WilliamsRIndicator(numFactory, barCount)

        assertThat(williamsR.lag).isEqualTo(barCount)
    }

    @Test
    fun `should throw exception for invalid bar count`() {
        assertThatThrownBy {
            WilliamsRIndicator(org.ta4j.core.num.DoubleNumFactory, 0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Bar count must be positive")

        assertThatThrownBy {
            WilliamsRIndicator(org.ta4j.core.num.DoubleNumFactory, -1)
        }.isInstanceOf(NegativeArraySizeException::class.java)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with single bar period`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(
                listOf(
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(1),
                        Instant.ofEpochSecond(2),
                        10.0,
                        20.0,
                        5.0,
                        15.0,
                        1000.0
                    )
                )
            )

        val williamsR = WilliamsRIndicator(numFactory, 1)
        context.withIndicator(williamsR)

        // For single bar: %R = (20 - 15) / (20 - 5) * -100 = 5/15 * -100 = -33.33
        context
            .assertNext(-33.3333)
            .assertIsStable()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should produce values in expected range`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 35.0, 30.0, 25.0)

        val williamsR = WilliamsRIndicator(numFactory, 5)
        context.withIndicator(williamsR)

        context.fastForwardUntilStable()

        // Williams %R should always be between 0 and -100
        repeat(5) {
            context.advance()
            val value = williamsR.value.doubleValue()
            assertThat(value).isBetween(-100.0, 0.0)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have meaningful toString representation`(numFactory: NumFactory) {
        val williamsR = WilliamsRIndicator(numFactory, 14)

        assertThat(williamsR.toString()).contains("WilliamsR")
        assertThat(williamsR.toString()).contains("14")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle trending market scenario`(numFactory: NumFactory) {
        // Simulate uptrending market
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(
                listOf(
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(1),
                        Instant.ofEpochSecond(2),
                        10.0,
                        12.0,
                        9.0,
                        11.0,
                        1000.0
                    ),
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(2),
                        Instant.ofEpochSecond(3),
                        11.0,
                        14.0,
                        10.0,
                        13.0,
                        1000.0
                    ),
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(3),
                        Instant.ofEpochSecond(4),
                        13.0,
                        16.0,
                        12.0,
                        15.0,
                        1000.0
                    ),
                    CandleReceived(
                        TimeFrame.DAY,
                        Instant.ofEpochSecond(4),
                        Instant.ofEpochSecond(5),
                        15.0,
                        18.0,
                        14.0,
                        17.0,
                        1000.0
                    )
                )
            )

        val williamsR = WilliamsRIndicator(numFactory, 3)
        context.withIndicator(williamsR)

        context.fastForward(3) // Get to stable state
        val firstValue = williamsR.value.doubleValue()

        context.advance() // One more bar in uptrend
        val secondValue = williamsR.value.doubleValue()

        // In an uptrend, Williams %R should generally show less oversold conditions
        // (values closer to 0) as prices move higher within the range
        assertThat(firstValue).isBetween(-100.0, 0.0)
        assertThat(secondValue).isBetween(-100.0, 0.0)
    }
}
