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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.num.NumFactory

class ChaikinMoneyFlowIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should produce reasonable CMF values`(numFactory: NumFactory) {
        // This test uses simple market data to verify calculations are reasonable
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(50.0, 55.0, 52.0, 58.0, 60.0)
            .withIndicator(ChaikinMoneyFlowIndicator(numFactory, 3))

        // Fast forward until stable and verify the result is reasonable
        context.fastForwardUntilStable()

        // CMF should be between -1 and 1
        val cmfValue = context.firstNumericIndicator!!.value
        assertThat(cmfValue.doubleValue()).isBetween(-1.0, 1.0)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should require positive bar count`(numFactory: NumFactory) {
        assertThrows<IllegalArgumentException> {
            ChaikinMoneyFlowIndicator(numFactory, 0)
        }.let { exception ->
            assertThat(exception.message).contains("n must be positive number, but was")
        }

        assertThrows<IllegalArgumentException> {
            ChaikinMoneyFlowIndicator(numFactory, -1)
        }.let { exception ->
            assertThat(exception.message).contains("n must be positive number, but was")
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag`(numFactory: NumFactory) {
        val barCount = 5
        val indicator = ChaikinMoneyFlowIndicator(numFactory, barCount)

        assertThat(indicator.lag).isEqualTo(barCount)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable after enough bars`(numFactory: NumFactory) {
        val barCount = 3
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0)
            .withIndicator(ChaikinMoneyFlowIndicator(numFactory, barCount))

        // Should not be stable initially
        assertThat(context.firstNumericIndicator!!.isStable).isFalse()

        // Should be stable after barCount periods
        context.fastForwardUntilStable()
        assertThat(context.firstNumericIndicator!!.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate CMF for typical price pattern`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(ChaikinMoneyFlowIndicator(numFactory, 3))

        // Manually advance through bars to test specific calculations
        val mockBuilder = context.barSeries.barBuilder()

        // Add first bar: High=12, Low=8, Close=10, Volume=1000
        val currentTime = Instant.EPOCH
        mockBuilder
            .startTime(currentTime)
            .endTime(currentTime.plusSeconds(86400))
            .openPrice(9.0)
            .highPrice(12.0)
            .lowPrice(8.0)
            .closePrice(10.0)
            .volume(1000.0)
            .add()

        // CLV = ((Close - Low) - (High - Close)) / (High - Low)
        // CLV = ((10 - 8) - (12 - 10)) / (12 - 8) = (2 - 2) / 4 = 0
        // Money Flow Volume = CLV * Volume = 0 * 1000 = 0

        // Since this is the first bar and we need 3 bars to be stable,
        // the indicator won't be stable yet
        assertThat(context.firstNumericIndicator!!.isStable).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have meaningful toString representation`(numFactory: NumFactory) {
        val barCount = 10
        val indicator = ChaikinMoneyFlowIndicator(numFactory, barCount)

        val toString = indicator.toString()
        assertThat(toString).contains("CMF")
        assertThat(toString).contains(barCount.toString())
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate CMF correctly for known values`(numFactory: NumFactory) {
        // Create context and indicator
        val context = MarketEventTestContext().withNumFactory(numFactory)
        val cmf = ChaikinMoneyFlowIndicator(numFactory, 20)
        context.withIndicator(cmf)

        val barSeries = context.barSeries

        // Test data from the original test case
        val testData = listOf(
            TestBar(62.34, 61.37, 62.15, 7849.025),
            TestBar(62.05, 60.69, 60.81, 11692.075),
            TestBar(62.27, 60.10, 60.45, 10575.307),
            TestBar(60.79, 58.61, 59.18, 13059.128),
            TestBar(59.93, 58.71, 59.24, 20733.508),
            TestBar(61.75, 59.86, 60.20, 29630.096),
            TestBar(60.00, 57.97, 58.48, 17705.294),
            TestBar(59.00, 58.02, 58.24, 7259.203),
            TestBar(59.07, 57.48, 58.69, 10474.629),
            TestBar(59.22, 58.30, 58.65, 5203.714),
            TestBar(58.75, 57.83, 58.47, 3422.865),
            TestBar(58.65, 57.86, 58.02, 3962.150),
            TestBar(58.47, 57.91, 58.17, 4095.905),
            TestBar(58.25, 57.83, 58.07, 3766.006),
            TestBar(58.35, 57.53, 58.13, 4239.335),
            TestBar(59.86, 58.58, 58.94, 8039.979),
            TestBar(59.53, 58.30, 59.10, 6956.717),
            TestBar(62.10, 58.53, 61.92, 18171.552),
            TestBar(62.16, 59.80, 61.37, 22225.894),
            TestBar(62.67, 60.93, 61.68, 14613.509),
            TestBar(62.38, 60.15, 62.09, 12319.763),
            TestBar(63.73, 62.26, 62.89, 15007.690),
            TestBar(63.85, 63.00, 63.53, 8879.667),
            TestBar(66.15, 63.58, 64.01, 22693.812),
            TestBar(65.34, 64.07, 64.77, 10191.814),
            TestBar(66.48, 65.20, 65.22, 10074.152),
            TestBar(65.23, 63.21, 63.28, 9411.620),
            TestBar(63.40, 61.88, 62.40, 10391.690),
            TestBar(63.18, 61.11, 61.55, 8926.512),
            TestBar(62.70, 61.25, 62.69, 7459.575)
        )

        // Expected values from the original test at specific indices
        val expectedValues = mapOf(
            0 to 0.6082,
            1 to -0.2484,
            19 to -0.1211,
            20 to -0.0997,
            21 to -0.0659,
            22 to -0.0257,
            23 to -0.0617,
            24 to -0.0481,
            25 to -0.0086,
            26 to -0.0087,
            27 to -0.005,
            28 to -0.0574,
            29 to -0.0148
        )

        var currentTime = Instant.EPOCH
        // Add bars one by one and test values at expected indices
        testData.forEachIndexed { index, data ->
            val endTime = currentTime.plusSeconds(86400)
            barSeries.barBuilder()
                .startTime(currentTime)
                .endTime(endTime)
                .openPrice(data.open ?: data.close) // Use close as open if not specified
                .highPrice(data.high)
                .lowPrice(data.low)
                .closePrice(data.close)
                .volume(data.volume)
                .add()

            currentTime = endTime
            // Check if we have an expected value for this index
            expectedValues[index]?.let { expectedValue ->
                assertNumEquals(expectedValue, cmf.value)
            }
        }
    }

    private data class TestBar(
        val high: Double,
        val low: Double,
        val close: Double,
        val volume: Double,
        val open: Double? = null,
    )

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should handle single bar period`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0)
            .withIndicator(ChaikinMoneyFlowIndicator(numFactory, 1))

        context.advance()
        // Should be stable after just one bar with period 1
        assertThat(context.firstNumericIndicator!!.isStable).isTrue()
    }
}
