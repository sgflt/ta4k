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
package org.ta4j.core.indicators.helpers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NaN
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider

class DifferencePercentageIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate percentage difference with zero threshold`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 121.0, 108.9)
            .withIndicator(
                DifferencePercentageIndicator(
                    Indicators.extended(numFactory).closePrice(),
                    numFactory.zero()
                )
            )

        // First value should be 0%
        context.assertNext(0.0)

        // 110 from 100 = 10% increase
        context.assertNext(10.0)

        // 121 from 110 = 10% increase
        context.assertNext(10.0)

        // 108.9 from 121 = -10% decrease
        context.assertNext(-10.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should use threshold to update notification point`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 108.0, 115.0, 112.0)
            .withIndicator(
                DifferencePercentageIndicator(
                    Indicators.extended(numFactory).closePrice(),
                    numFactory.numOf(10.0) // 10% threshold
                )
            )

        // First value is 0%
        context.assertNext(0.0)

        // 105 from 100 = 5% (below threshold, notification stays at 100)
        context.assertNext(5.0)

        // 108 from 100 = 8% (still below threshold)
        context.assertNext(8.0)

        // 115 from 100 = 15% (exceeds threshold, notification updates to 115)
        context.assertNext(15.0)

        // 112 from 115 = -2.6% (new base is 115)
        context.fastForward(1)
        val lastValue = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(lastValue).isCloseTo(-2.6087, Offset.offset(0.01))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero values and return NaN`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(0.0, 100.0, 0.0)
            .withIndicator(
                DifferencePercentageIndicator(
                    Indicators.extended(numFactory).closePrice()
                )
            )

        // Zero value should return NaN
        context.assertNextNaN()

        // Valid value after zero - first valid becomes base (0%)
        context.assertNext(0.0)

        // Zero again should return NaN
        context.assertNextNaN()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle negative values correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(-100.0, -110.0, -90.0)
            .withIndicator(
                DifferencePercentageIndicator(
                    Indicators.extended(numFactory).closePrice()
                )
            )

        // First value is 0%
        context.assertNext(0.0)

        // -110 from -100 = 10% increase (more negative)
        context.assertNext(10.0)

        // -90 from -110 = -18.18% decrease (less negative)
        context.fastForward(1)
        val lastValue = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(lastValue).isCloseTo(-18.18, Offset.offset(0.01))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should use Number constructor correctly`(numFactory: NumFactory) {
        val indicator = DifferencePercentageIndicator(
            Indicators.extended(numFactory).closePrice(),
            5.0 // Number threshold
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 103.0, 106.0)
            .withIndicator(indicator)

        context.assertNext(0.0)
        context.assertNext(3.0) // Below 5% threshold
        context.assertNext(6.0) // Exceeds 5% threshold, updates notification
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle large percentage changes`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(1.0, 2.0, 0.5, 4.0)
            .withIndicator(
                DifferencePercentageIndicator(
                    Indicators.extended(numFactory).closePrice()
                )
            )

        context.assertNext(0.0)    // First value
        context.assertNext(100.0)  // 2.0 from 1.0 = 100% increase
        context.assertNext(-75.0)  // 0.5 from 2.0 = 75% decrease
        context.assertNext(700.0)  // 4.0 from 0.5 = 700% increase
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should maintain threshold behavior with multiple crossings`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 120.0, 125.0, 110.0, 130.0)
            .withIndicator(
                DifferencePercentageIndicator(
                    Indicators.extended(numFactory).closePrice(),
                    numFactory.numOf(20.0) // 20% threshold
                )
            )

        context.assertNext(0.0)   // First value
        context.assertNext(20.0)  // 120 from 100 = 20%, meets threshold, updates to 120
        context.assertNext(4.16666) // 125 from 120 ≈ 4.17%
        context.fastForward(1)
        val fourthValue = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(fourthValue).isCloseTo(-8.33, Offset.offset(0.01)) // 110 from 120

        context.fastForward(1)
        val fifthValue = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(fifthValue).isCloseTo(8.33, Offset.offset(0.01)) // 130 from 120
    }

    @Test
    fun `should reject NaN threshold in constructor`() {
        val numFactory = NumFactoryProvider.defaultNumFactory

        assertThatThrownBy {
            DifferencePercentageIndicator(
                Indicators.extended(numFactory).closePrice(),
                NaN
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Percentage threshold cannot be NaN")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag and stability properties`(numFactory: NumFactory) {
        val indicator = DifferencePercentageIndicator(
            Indicators.extended(numFactory).closePrice()
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 110.0, 120.0)
            .withIndicator(indicator)

        // Lag should be 1
        assertThat(indicator.lag).isEqualTo(1)

        // Should not be stable initially
        assertThat(indicator.isStable).isFalse()

        // Should become stable after first valid value
        context.fastForward(1)
        assertThat(indicator.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle threshold with absolute values correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 95.0, 110.0, 85.0)
            .withIndicator(
                DifferencePercentageIndicator(
                    Indicators.extended(numFactory).closePrice(),
                    numFactory.numOf(10.0) // 10% threshold
                )
            )

        context.assertNext(0.0)   // First value
        context.assertNext(-5.0)  // 95 from 100 = -5% (below 10% absolute threshold)
        context.assertNext(10.0)  // 110 from 100 = 10% (meets threshold)

        // Now notification is at 110
        context.fastForward(1)
        val lastValue = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(lastValue).isCloseTo(-22.73, Offset.offset(0.01)) // 85 from 110
    }

    @Test
    fun `should use correct string representation`() {
        val numFactory = NumFactoryProvider.defaultNumFactory
        val indicator = DifferencePercentageIndicator(
            Indicators.extended(numFactory).closePrice(),
            numFactory.numOf(5.0)
        )

        assertThat(indicator.toString()).contains("DifferencePercentage")
        assertThat(indicator.toString()).contains("5")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle constant values correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 100.0, 100.0, 100.0)
            .withIndicator(
                DifferencePercentageIndicator(
                    Indicators.extended(numFactory).closePrice()
                )
            )

        // All values should be 0% since prices don't change
        repeat(4) {
            context.assertNext(0.0)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle mixed scenarios with various thresholds`(numFactory: NumFactory) {
        val prices = doubleArrayOf(50.0, 55.0, 45.0, 60.0, 48.0, 54.0)
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*prices)

        // Test with 5% threshold
        val indicator5 = DifferencePercentageIndicator(
            Indicators.extended(numFactory).closePrice(),
            numFactory.numOf(5.0)
        )

        // Test with 15% threshold
        val indicator15 = DifferencePercentageIndicator(
            Indicators.extended(numFactory).closePrice(),
            numFactory.numOf(15.0)
        )

        context.withIndicator(indicator5, "diff5")
            .withIndicator(indicator15, "diff15")

        context.assertNext(0.0)  // Both start at 0%
        context.onIndicator("diff15").assertCurrent(0.0)

        context.assertNext(10.0)  // 55 from 50 = 10% (exceeds 5% threshold)
        context.onIndicator("diff15").assertCurrent(10.0) // Same base for 15%

        context.assertNext(-18.1818)  // 45 from 55 = -18.18% for 5%, -10% for 15%
        // For 15% threshold, base is still 50 since 10% didn't exceed threshold
        context.onIndicator("diff15").assertCurrent(-10.0) // 45 from 50
    }
}
