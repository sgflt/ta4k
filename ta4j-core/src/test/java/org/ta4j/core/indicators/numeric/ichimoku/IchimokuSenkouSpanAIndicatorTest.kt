/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.indicators.numeric.ichimoku

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

class IchimokuSenkouSpanAIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCalculation(numFactory: NumFactory) {
        // Create custom indicators with small bar counts for testing
        val indicator = IchimokuSenkouSpanAIndicator(
            numFactory,
            tenkanSenBarCount = 2,
            kijunSenBarCount = 3,
            offset = 0
        ) // offset=0 for simplicity

        // Create bars with known values
        val bars = listOf(
            createBar(numFactory, high = 10.0, low = 5.0),   // Tenkan: (10+5)/2=7.5, Kijun: (10+5)/2=7.5
            createBar(numFactory, high = 12.0, low = 6.0),   // Tenkan: (12+5)/2=8.5, Kijun: (12+5)/2=8.5
            createBar(numFactory, high = 8.0, low = 4.0),    // Tenkan: (12+4)/2=8.0, Kijun: (12+4)/2=8.0
            createBar(numFactory, high = 14.0, low = 7.0)    // Tenkan: (14+4)/2=9.0, Kijun: (14+4)/2=9.0
        )

        for (bar in bars) {
            indicator.onBar(bar)
        }

        // Senkou Span A = (Tenkan + Kijun) / 2 = (9.0 + 9.0) / 2 = 9.0
        assertNumEquals(9.0, indicator.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testDefaultConstructor(numFactory: NumFactory) {
        val indicator = IchimokuSenkouSpanAIndicator(numFactory)

        // Should use default Tenkan (9) and Kijun (26) periods
        // Lag should include offset (26) plus max of underlying indicators' lags
        assertThat(indicator.lag).isEqualTo(26 + 26) // offset + max(9, 26)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCustomTimeFrames(numFactory: NumFactory) {
        val indicator = IchimokuSenkouSpanAIndicator(numFactory, 5, 10)

        // Should use custom Tenkan (5) and Kijun (10) periods with default offset (26)
        assertThat(indicator.lag).isEqualTo(26 + 10) // offset + max(5, 10)
    }

    @Test
    fun testInitialValue() {
        val numFactory = DoubleNumFactory
        val indicator = IchimokuSenkouSpanAIndicator(numFactory)

        // Before any bars, value should be NaN
        assertThat(indicator.value).isEqualTo(NaN)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testStability(numFactory: NumFactory) {
        val indicator = IchimokuSenkouSpanAIndicator(numFactory, 2, 3)

        // Add enough bars to make underlying indicators stable
        repeat(5) { i ->
            val bar = createBar(numFactory, high = 10.0 + i, low = 5.0 + i)
            indicator.onBar(bar)
        }

        assertThat(indicator.isStable).isTrue()
    }

    private fun createBar(numFactory: NumFactory, high: Double, low: Double): Bar {
        return object : Bar {
            override val timeFrame = TimeFrame.DAY
            override val timePeriod = Duration.ofDays(1)
            override val beginTime = Instant.now()
            override val endTime = Instant.now().plusSeconds(3600)
            override val openPrice: Num = numFactory.numOf((high + low) / 2)
            override val highPrice: Num = numFactory.numOf(high)
            override val lowPrice: Num = numFactory.numOf(low)
            override val closePrice: Num = numFactory.numOf((high + low) / 2)
            override val volume: Num = numFactory.numOf(1000)
        }
    }
}
