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

class IchimokuChikouSpanIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testDefaultConstructor(numFactory: NumFactory) {
        val indicator = IchimokuChikouSpanIndicator(numFactory)

        // Should use default timeDelay (26)
        assertThat(indicator.lag).isEqualTo(26)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCustomTimeDelay(numFactory: NumFactory) {
        val timeDelay = 10
        val indicator = IchimokuChikouSpanIndicator(numFactory, timeDelay)

        assertThat(indicator.lag).isEqualTo(timeDelay)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCalculation(numFactory: NumFactory) {
        val timeDelay = 3
        val indicator = IchimokuChikouSpanIndicator(numFactory, timeDelay)

        // Create bars with known close prices
        val closePrices = listOf(10.0, 15.0, 20.0, 25.0, 30.0, 35.0)
        val bars = closePrices.map { closePrice ->
            createBar(numFactory, closePrice)
        }

        for (bar in bars) {
            indicator.onBar(bar)
        }

        // After 6 bars with timeDelay=3, current value should be the close price from 3 periods ago
        // Current bar index: 5 (0-based), timeDelay: 3, so should get price from index 2 (20.0)
        assertNumEquals(20.0, indicator.value)
    }

    @Test
    fun testInitialValue() {
        val numFactory = DoubleNumFactory
        val indicator = IchimokuChikouSpanIndicator(numFactory)

        // Before any bars, value should be NaN
        assertThat(indicator.value).isEqualTo(NaN)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testStability(numFactory: NumFactory) {
        val timeDelay = 3
        val indicator = IchimokuChikouSpanIndicator(numFactory, timeDelay)

        // Should not be stable initially
        assertThat(indicator.isStable).isFalse()

        // Add bars one by one
        repeat(timeDelay) { i ->
            val bar = createBar(numFactory, 10.0 + i)
            indicator.onBar(bar)
            assertThat(indicator.isStable).isFalse() // Still not stable
        }

        // Add one more bar to make it stable
        val bar = createBar(numFactory, 10.0 + timeDelay)
        indicator.onBar(bar)
        assertThat(indicator.isStable).isTrue() // Now it should be stable
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testValueBeforeStable(numFactory: NumFactory) {
        val timeDelay = 3
        val indicator = IchimokuChikouSpanIndicator(numFactory, timeDelay)

        // Add less than timeDelay + 1 bars
        repeat(timeDelay) { i ->
            val bar = createBar(numFactory, 10.0 + i)
            indicator.onBar(bar)
            // Value should be NaN since we don't have enough history
            assertThat(indicator.value).isEqualTo(NaN)
        }
    }

    private fun createBar(numFactory: NumFactory, closePrice: Double): Bar {
        return object : Bar {
            override val timeFrame = TimeFrame.DAY
            override val timePeriod = Duration.ofDays(1)
            override val beginTime = Instant.now()
            override val endTime = Instant.now().plusSeconds(3600)
            override val openPrice: Num = numFactory.numOf(closePrice)
            override val highPrice: Num = numFactory.numOf(closePrice + 1)
            override val lowPrice: Num = numFactory.numOf(closePrice - 1)
            override val closePrice: Num = numFactory.numOf(closePrice)
            override val volume: Num = numFactory.numOf(1000)
        }
    }
}
