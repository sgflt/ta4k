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

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory

class ROCVIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate ROCV correctly with period of 3`(numFactory: NumFactory) {
        val rocv = ROCVIndicator(numFactory, 3)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(rocv)

        val series = context.barSeries

        // Add bars with specific volumes
        addBarWithVolume(series, 0, 100.0)  // Bar 0: Volume = 100
        context.assertCurrent(0.0)          // Not stable yet

        addBarWithVolume(series, 1, 120.0)  // Bar 1: Volume = 120
        context.assertCurrent(0.0)          // Not stable yet

        addBarWithVolume(series, 2, 150.0)  // Bar 2: Volume = 150
        context.assertCurrent(0.0)          // Not stable yet

        addBarWithVolume(series, 3, 200.0)  // Bar 3: Volume = 200, compare with bar 0 (100)
        // ROCV = (200 - 100) / 100 * 100 = 100%
        context.assertCurrent(100.0)

        addBarWithVolume(series, 4, 60.0)   // Bar 4: Volume = 60, compare with bar 1 (120)
        // ROCV = (60 - 120) / 120 * 100 = -50%
        context.assertCurrent(-50.0)

        addBarWithVolume(series, 5, 225.0)  // Bar 5: Volume = 225, compare with bar 2 (150)
        // ROCV = (225 - 150) / 150 * 100 = 50%
        context.assertCurrent(50.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle zero volume gracefully`(numFactory: NumFactory) {
        val rocv = ROCVIndicator(numFactory, 1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(rocv)

        val series = context.barSeries

        addBarWithVolume(series, 0, 0.0)    // Bar 0: Volume = 0
        context.assertCurrent(0.0)          // Not stable yet

        addBarWithVolume(series, 1, 100.0)  // Bar 1: Volume = 100, compare with bar 0 (0)
        // When previous volume is 0, should return 0 to avoid division by zero
        context.assertCurrent(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle equal volumes`(numFactory: NumFactory) {
        val rocv = ROCVIndicator(numFactory, 2)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(rocv)

        val series = context.barSeries

        addBarWithVolume(series, 0, 100.0)  // Bar 0: Volume = 100
        context.assertCurrent(0.0)          // Not stable yet

        addBarWithVolume(series, 1, 150.0)  // Bar 1: Volume = 150
        context.assertCurrent(0.0)          // Not stable yet

        addBarWithVolume(series, 2, 100.0)  // Bar 2: Volume = 100, compare with bar 0 (100)
        // ROCV = (100 - 100) / 100 * 100 = 0%
        context.assertCurrent(0.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle large volume increases and decreases`(numFactory: NumFactory) {
        val rocv = ROCVIndicator(numFactory, 1)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(rocv)

        val series = context.barSeries

        addBarWithVolume(series, 0, 50.0)   // Bar 0: Volume = 50
        context.assertCurrent(0.0)          // Not stable yet

        addBarWithVolume(series, 1, 250.0)  // Bar 1: Volume = 250, compare with bar 0 (50)
        // ROCV = (250 - 50) / 50 * 100 = 400%
        context.assertCurrent(400.0)

        addBarWithVolume(series, 2, 10.0)   // Bar 2: Volume = 10, compare with bar 1 (250)
        // ROCV = (10 - 250) / 250 * 100 = -96%
        context.assertCurrent(-96.0)
    }

    @Test
    fun `should validate constructor parameters`() {
        assertThatThrownBy { ROCVIndicator(DoubleNumFactory, 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("n must be positive number, but was")

        assertThatThrownBy { ROCVIndicator(DoubleNumFactory, -1) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("n must be positive number, but was")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag and stability properties`(numFactory: NumFactory) {
        val rocv = ROCVIndicator(numFactory, 5)

        assertThat(rocv.lag).isEqualTo(5)
        assertThat(rocv.isStable).isFalse()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(rocv)

        val series = context.barSeries

        // Should be unstable for first 5 bars
        repeat(5) { i ->
            addBarWithVolume(series, i, 100.0 + i * 10)
            assertThat(rocv.isStable).isFalse()
        }

        // Should be stable from 6th bar onwards
        addBarWithVolume(series, 5, 150.0)
        assertThat(rocv.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should return correct string representation`(numFactory: NumFactory) {
        val rocv = ROCVIndicator(numFactory, 10)
        assertThat(rocv.toString()).contains("ROCV(10)")
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate ROCV for different periods correctly`(numFactory: NumFactory) {
        // Test with period 1
        val rocv1 = ROCVIndicator(numFactory, 1)
        val context1 = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(rocv1)

        val series1 = context1.barSeries
        addBarWithVolume(series1, 0, 100.0)
        context1.assertCurrent(0.0)         // Not stable yet

        addBarWithVolume(series1, 1, 150.0) // Compare with previous bar: (150-100)/100*100 = 50%
        context1.assertCurrent(50.0)

        // Test with period 2
        val rocv2 = ROCVIndicator(numFactory, 2)
        val context2 = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(rocv2)

        val series2 = context2.barSeries
        addBarWithVolume(series2, 0, 100.0)
        context2.assertCurrent(0.0)         // Not stable yet

        addBarWithVolume(series2, 1, 150.0)
        context2.assertCurrent(0.0)         // Not stable yet

        addBarWithVolume(series2, 2, 180.0) // Compare with bar 0: (180-100)/100*100 = 80%
        context2.assertCurrent(80.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should demonstrate fluent usage with context`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0, 15.0)

        // Create ROCV indicator after setting up context
        val rocv = ROCVIndicator(numFactory, 3)

        context.withIndicator(rocv)

        // ROCV will use default volume from withCandlePrices (NaN initially)
        // This test demonstrates the pattern, though volume testing requires custom bars
        context
            .assertNext(0.0)    // Bar 0: Not stable
            .assertNext(0.0)    // Bar 1: Not stable
            .assertNext(0.0)    // Bar 2: Not stable
            .assertNext(0.0)    // Bar 3: Stable but volume comparison with NaN
    }

    private fun addBarWithVolume(series: BarSeries, index: Int, volume: Double) {
        series.barBuilder()
            .startTime(Instant.ofEpochSecond(index.toLong()))
            .endTime(Instant.ofEpochSecond(index.toLong() + 1))
            .openPrice(10.0 + index)
            .highPrice(11.0 + index)
            .lowPrice(9.0 + index)
            .closePrice(10.5 + index)
            .volume(volume)
            .add()
    }
}
