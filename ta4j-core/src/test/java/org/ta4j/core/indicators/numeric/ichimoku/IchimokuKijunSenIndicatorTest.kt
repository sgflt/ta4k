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
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

class IchimokuKijunSenIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testDefaultBarCount(numFactory: NumFactory) {
        val indicator = IchimokuKijunSenIndicator(numFactory)

        // Default bar count should be 26
        assertThat(indicator.lag).isEqualTo(26)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCustomBarCount(numFactory: NumFactory) {
        val customBarCount = 10
        val indicator = IchimokuKijunSenIndicator(numFactory, customBarCount)

        assertThat(indicator.lag).isEqualTo(customBarCount)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testInheritsFromIchimokuLineIndicator(numFactory: NumFactory) {
        val indicator = IchimokuKijunSenIndicator(numFactory, 5)

        // Should behave like IchimokuLineIndicator - average of highest high and lowest low
        val bars = listOf(
            createBar(numFactory, high = 10.0, low = 5.0),
            createBar(numFactory, high = 12.0, low = 6.0),
            createBar(numFactory, high = 8.0, low = 4.0),
            createBar(numFactory, high = 15.0, low = 7.0),
            createBar(numFactory, high = 11.0, low = 9.0)
        )

        for (bar in bars) {
            indicator.onBar(bar)
        }

        // Over last 5 bars: highest high = 15, lowest low = 4
        // Expected = (15 + 4) / 2 = 9.5
        assertNumEquals(9.5, indicator.value)
    }

    @Test
    fun testIsIchimokuLineIndicator() {
        val numFactory = DoubleNumFactory
        val indicator = IchimokuKijunSenIndicator(numFactory)

        assertThat(indicator).isInstanceOf(IchimokuLineIndicator::class.java)
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
