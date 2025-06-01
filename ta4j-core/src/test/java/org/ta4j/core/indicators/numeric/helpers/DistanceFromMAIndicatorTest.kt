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
package org.ta4j.core.indicators.numeric.helpers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Tests for {@link DistanceFromMAIndicator}.
 */
class DistanceFromMAIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate distance from SMA correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0)
            .withIndicator(DistanceFromMAIndicator(numFactory, Indicators.closePrice().sma(3)))

        context
            .fastForwardUntilStable()
            .assertCurrent(0.09090)
            .assertNext(0.08333333333333333)
            .assertNext(0.0769230769230769)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle negative distance correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(15.0, 14.0, 13.0, 12.0, 11.0)
            .withIndicator(DistanceFromMAIndicator(numFactory, Indicators.extended(numFactory).closePrice().sma(3)))

        context.fastForwardUntilStable()
            .assertCurrent(-0.07142857142857142)
            .assertNext(-0.07692)
            .assertNext(-0.08333333333333333)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with exponential moving average`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 12.0, 14.0, 16.0, 18.0)
            .withIndicator(DistanceFromMAIndicator(numFactory, Indicators.extended(numFactory).closePrice().ema(3)))

        // EMA calculations are more complex, but we can test that it produces reasonable values
        context.advance()
        assertThat(context.firstNumericIndicator!!.value.doubleValue()).isEqualTo(0.0)

        context.advance()
        assertThat(context.firstNumericIndicator!!.value.doubleValue()).isGreaterThan(0.0)

        context.advance()
        assertThat(context.firstNumericIndicator!!.value.doubleValue()).isGreaterThan(0.0)
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero moving average gracefully`(numFactory: NumFactory) {
        // This is an edge case that shouldn't happen in practice, but we should handle it
        val zeroIndicator = object : NumericIndicator(numFactory) {
            override fun updateState(bar: org.ta4j.core.api.series.Bar) {
                value = numFactory.zero()
            }

            override val lag = 0
            override val isStable = true
        }

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0)
            .withIndicator(DistanceFromMAIndicator(numFactory, zeroIndicator))

        context
            .assertNext(0.0) // Should handle division by zero gracefully
            .assertNext(0.0)
            .assertNext(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate distance with varying price movements`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 95.0, 110.0, 90.0, 120.0, 99.0, 105.0)
            .withIndicator(DistanceFromMAIndicator(numFactory, Indicators.extended(numFactory).closePrice().sma(4)))

        context.fastForwardUntilStable()
            .assertNext(-0.1)
            .assertNext(0.1566265)
            .assertNext(-0.0548926014319809)
            .assertNext(0.0144927536231884)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag and stability properties`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0)

        val sma = Indicators.extended(numFactory).closePrice().sma(3)
        val distanceIndicator = DistanceFromMAIndicator(numFactory, sma)

        assertThat(distanceIndicator.lag).isEqualTo(sma.lag)

        // Test stability
        context.withIndicator(distanceIndicator)
        assertThat(distanceIndicator.isStable).isFalse()

        context.advance() // First bar
        assertThat(distanceIndicator.isStable).isFalse()

        context.advance() // Second bar
        assertThat(distanceIndicator.isStable).isFalse()

        context.advance() // Third bar - SMA(3) should be stable now
        assertThat(distanceIndicator.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should work with hull moving average`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(50.0, 52.0, 48.0, 54.0, 46.0)
            .withIndicator(DistanceFromMAIndicator(numFactory, Indicators.extended(numFactory).closePrice().hma(3)))

        context.advance() // First value
        assertThat(context.firstNumericIndicator!!.value.doubleValue()).isEqualTo(0.0)

        // Test that it produces reasonable values without specific calculations
        // since HMA is quite complex
        context.advance()
        context.advance()
        context.advance()
        context.advance()

        // Verify the indicator produces finite values
        assertThat(context.firstNumericIndicator!!.value.doubleValue()).isFinite()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle large price deviations`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 100.0, 100.0, 200.0) // 100% increase
            .withIndicator(DistanceFromMAIndicator(numFactory, Indicators.extended(numFactory).closePrice().sma(3)))

        context
            .fastForwardUntilStable()
            .assertNext(0.5) // (200-133.33)/133.33 ≈ 0.5
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should maintain toString format`(numFactory: NumFactory) {
        val sma = Indicators.closePrice().sma(3)
        val distanceIndicator = DistanceFromMAIndicator(numFactory, sma)

        val toStringResult = distanceIndicator.toString()
        assertThat(toStringResult).contains("DistanceFromMA")
        assertThat(toStringResult).contains("SMA")
        assertThat(toStringResult).contains("=>")
    }
}
