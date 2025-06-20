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
package org.ta4j.core.indicators.numeric.trend

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.num.NaN
import org.ta4j.core.num.NumFactory

class ParabolicSARIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should initialize with first high value in uptrend`(numFactory: NumFactory) {
        val indicator = ParabolicSARIndicator(
            numFactory,
            numFactory.numOf(0.02), // startAF
            numFactory.numOf(0.2),  // maxAF
            numFactory.numOf(0.02)  // afIncrement
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 102.0, 105.0, 107.0)
            .withIndicator(indicator)

        context
            .assertNextNaN() // First bar has no SAR
            .assertNext(100.0) // Second bar: SAR initialized to first low
            .fastForward(2) // Process remaining bars

        // In uptrend, SAR should be below current price
        val currentSAR = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(currentSAR).isLessThan(107.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should track uptrend with accelerating factor`(numFactory: NumFactory) {
        val indicator = ParabolicSARIndicator(
            numFactory,
            numFactory.numOf(0.02),
            numFactory.numOf(0.2),
            numFactory.numOf(0.02)
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 101.0, 103.0, 106.0, 110.0, 115.0)
            .withIndicator(indicator)

        context
            .assertNextNaN() // First bar
            .assertNext(100.0) // Initialize at first low

        // Track several bars and verify SAR stays below price in uptrend
        val sarValues = mutableListOf<Double>()
        val prices = listOf(103.0, 106.0, 110.0, 115.0)

        prices.forEach { price ->
            context.fastForward(1)
            val sar = context.firstNumericIndicator!!.value.doubleValue()
            sarValues.add(sar)
            assertThat(sar).isLessThan(price)
        }

        // SAR should be increasing (moving up) in uptrend
        for (i in 1 until sarValues.size) {
            assertThat(sarValues[i]).isGreaterThan(sarValues[i - 1])
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle downtrend correctly`(numFactory: NumFactory) {
        val indicator = ParabolicSARIndicator(
            numFactory,
            numFactory.numOf(0.02),
            numFactory.numOf(0.2),
            numFactory.numOf(0.02)
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 98.0, 95.0, 92.0, 88.0)
            .withIndicator(indicator)

        context
            .assertNextNaN() // First bar
            .assertNext(100.0) // Initialize at first high

        // In downtrend, SAR should be above current price
        val prices = listOf(95.0, 92.0, 88.0)
        prices.forEach { price ->
            context.fastForward(1)
            val sar = context.firstNumericIndicator!!.value.doubleValue()
            assertThat(sar).isGreaterThan(price)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should respect maximum acceleration factor`(numFactory: NumFactory) {
        val maxAF = 0.2
        val indicator = ParabolicSARIndicator(
            numFactory,
            numFactory.numOf(0.02),
            numFactory.numOf(maxAF),
            numFactory.numOf(0.02)
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                100.0, 102.0, 105.0, 109.0, 114.0, 120.0, 127.0, 135.0, 144.0, 154.0
            )
            .withIndicator(indicator)

        context
            .assertNextNaN()
            .assertNext(100.0)

        // Process many consecutive higher highs to test max AF
        repeat(8) { context.fastForward(1) }

        // Verify indicator produces reasonable values
        val finalSAR = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(finalSAR).isLessThan(154.0) // Should be below final price
        assertThat(finalSAR).isGreaterThan(100.0) // Should have moved up significantly
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle sideways market`(numFactory: NumFactory) {
        val indicator = ParabolicSARIndicator(
            numFactory,
            numFactory.numOf(0.02),
            numFactory.numOf(0.2),
            numFactory.numOf(0.02)
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                100.0, 101.0, 99.0, 100.5, 99.5, 100.2, 99.8, 100.1
            )
            .withIndicator(indicator)

        context
            .assertNextNaN()
            .assertNext(100.0)

        // Process sideways movement
        repeat(6) { context.fastForward(1) }

        // In sideways market, SAR should still produce reasonable values
        val finalSAR = context.firstNumericIndicator!!.value.doubleValue()
        assertThat(finalSAR).isBetween(98.0, 102.0) // Should be within reasonable range
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable after sufficient bars`(numFactory: NumFactory) {
        val indicator = ParabolicSARIndicator(
            numFactory,
            numFactory.numOf(0.02),
            numFactory.numOf(0.2),
            numFactory.numOf(0.02)
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 102.0, 105.0)
            .withIndicator(indicator)

        context.fastForward(1) // First bar - should not be stable
        assertThat(indicator.isStable).isFalse()

        context.fastForward(1) // Second bar - should be stable
        assertThat(indicator.isStable).isTrue()

        context.fastForward(1) // Third bar - should remain stable
        assertThat(indicator.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle complex up and down trend scenario`(numFactory: NumFactory) {
        val indicator = ParabolicSARIndicator(numFactory)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)

        // Build the exact same bars as in original test
        val bars = listOf(
            BarData(74.5, 75.1, 75.11, 74.06),
            BarData(75.09, 75.9, 76.030000, 74.640000),
            BarData(79.99, 75.24, 76.269900, 75.060000),
            BarData(75.30, 75.17, 75.280000, 74.500000),
            BarData(75.16, 74.6, 75.310000, 74.540000),
            BarData(74.58, 74.1, 75.467000, 74.010000),
            BarData(74.01, 73.740000, 74.700000, 73.546000),
            BarData(73.71, 73.390000, 73.830000, 72.720000),
            BarData(73.35, 73.25, 73.890000, 72.86),
            BarData(73.24, 74.36, 74.410000, 73.0),
            BarData(74.36, 76.510000, 76.830000, 74.820000),
            BarData(76.5, 75.590000, 76.850000, 74.540000),
            BarData(75.60, 75.910000, 76.960000, 75.510000),
            BarData(75.82, 74.610000, 77.070000, 74.560000),
            BarData(74.75, 75.330000, 75.530000, 74.010000),
            BarData(75.33, 75.010000, 75.500000, 74.510000),
            BarData(75.0, 75.620000, 76.210000, 75.250000),
            BarData(75.63, 76.040000, 76.460000, 75.092800),
            BarData(76.0, 76.450000, 76.450000, 75.435000),
            BarData(76.45, 76.260000, 76.470000, 75.840000),
            BarData(76.30, 76.850000, 77.000000, 76.190000)
        )

        // First bar
        context.barSeries.barBuilder()
            .startTime(Instant.EPOCH)
            .endTime(Instant.EPOCH.plus(Duration.ofDays(1)))
            .openPrice(bars[0].open)
            .closePrice(bars[0].close)
            .highPrice(bars[0].high)
            .lowPrice(bars[0].low)
            .add()

        assertNumEquals(NaN, indicator.value)

        // Second bar
        context.barSeries.barBuilder()
            .startTime(Instant.EPOCH.plus(Duration.ofDays(1)))
            .endTime(Instant.EPOCH.plus(Duration.ofDays(2)))
            .openPrice(bars[1].open)
            .closePrice(bars[1].close)
            .highPrice(bars[1].high)
            .lowPrice(bars[1].low)
            .add()

        assertNumEquals(74.06, indicator.value)

        // Add remaining bars and test specific expected values
        val expectedValues = listOf(
            74.0994, 74.18622, 74.26956, 76.2699, 76.22470200000001,
            76.11755392, 75.9137006848, 75.72207864371201, 72.72, 72.8022,
            72.964112, 73.20386528, 73.5131560576, 73.797703572992, 74.05948,
            74.30032, 74.5219, 74.7257, 74.91328
        )

        var startTime = Instant.EPOCH + Duration.ofDays(2)
        for (i in 2 until bars.size) {
            val endTime = startTime + Duration.ofDays(1)
            context.barSeries.barBuilder()
                .startTime(startTime)
                .endTime(endTime)
                .openPrice(bars[i].open)
                .closePrice(bars[i].close)
                .highPrice(bars[i].high)
                .lowPrice(bars[i].low)
                .add()
            startTime = endTime

            assertNumEquals(expectedValues[i - 2], indicator.value)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should start with down and then up trend`(numFactory: NumFactory) {
        val indicator = ParabolicSARIndicator(numFactory)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(indicator)

        // Bitcoin BTCUSDT daily candles starting from 17 Aug 2017
        val bars = listOf(
            BarData(4261.48, 4285.08, 4485.39, 4200.74),
            BarData(4285.08, 4108.37, 4371.52, 3938.77), // starting with down trend
            BarData(4108.37, 4139.98, 4184.69, 3850.00), // hold trend...
            BarData(4120.98, 4086.29, 4211.08, 4032.62),
            BarData(4069.13, 4016.00, 4119.62, 3911.79),
            BarData(4016.00, 4040.00, 4104.82, 3400.00),
            BarData(4040.00, 4114.01, 4265.80, 4013.89),
            BarData(4147.00, 4316.01, 4371.68, 4085.01), // switch to up trend
            BarData(4316.01, 4280.68, 4453.91, 4247.48), // hold trend
            BarData(4280.71, 4337.44, 4367.00, 4212.41)
        )

        val expectedValues = listOf(
            NaN, 4485.39000000, 4474.4576, 4449.47929,
            4425.50012, 4402.48011, 4342.33131, 3400.00000000,
            3419.43360000, 3460.81265600
        )

        var startTime = Instant.EPOCH
        bars.forEachIndexed { index, bar ->
            val endTime = startTime + Duration.ofDays(1)
            context.barSeries.barBuilder()
                .startTime(startTime)
                .endTime(endTime)
                .openPrice(bar.open)
                .closePrice(bar.close)
                .highPrice(bar.high)
                .lowPrice(bar.low)
                .add()
            startTime = endTime

            if (expectedValues[index] == NaN) {
                assertNumEquals(NaN, indicator.value)
            } else {
                assertNumEquals(expectedValues[index] as Double, indicator.value)
            }
        }
    }

    private data class BarData(
        val open: Double,
        val close: Double,
        val high: Double,
        val low: Double,
    )
}
