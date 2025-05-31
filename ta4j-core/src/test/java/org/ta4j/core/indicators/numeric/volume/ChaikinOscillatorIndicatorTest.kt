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
import kotlin.test.assertFailsWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class ChaikinOscillatorIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate Chaikin Oscillator with original test data`(numFactory: NumFactory) {
        val chaikin = ChaikinOscillatorIndicator(numFactory)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(chaikin)
            .withMarketEvents(
                listOf(
                    createCandle(0, 12.915, 13.600, 12.890, 13.550, 264266.0),
                    createCandle(1, 13.550, 13.770, 13.310, 13.505, 305427.0),
                    createCandle(2, 13.510, 13.590, 13.425, 13.490, 104077.0),
                    createCandle(3, 13.515, 13.545, 13.400, 13.480, 136135.0),
                    createCandle(4, 13.490, 13.495, 13.310, 13.345, 92090.0),
                    createCandle(5, 13.350, 13.490, 13.325, 13.420, 80948.0),
                    createCandle(6, 13.415, 13.460, 13.290, 13.300, 82983.0),
                    createCandle(7, 13.320, 13.320, 13.090, 13.130, 126918.0),
                    createCandle(8, 13.145, 13.225, 13.090, 13.150, 68560.0),
                    createCandle(9, 13.150, 13.250, 13.110, 13.245, 41178.0),
                    createCandle(10, 13.245, 13.250, 13.120, 13.210, 63606.0),
                    createCandle(11, 13.210, 13.275, 13.185, 13.275, 34402.0)
                )
            )

        // Test each expected value from original test
        context.assertNext(0.0)
        context.assertNext(-361315.15734265576)
        context.assertNext(-611288.0465670675)
        context.assertNext(-771681.707243684)
        context.assertNext(-1047600.3223165069)
        context.assertNext(-1128952.3867409695)
        context.assertNext(-1930922.241574394)
        context.assertNext(-2507483.932954022)
        context.assertNext(-2591747.9037044123)
        context.assertNext(-2404678.698472605)
        context.assertNext(-2147771.081319658)
        context.assertNext(-1858366.685091666)
    }

    private fun createCandle(
        index: Long,
        open: Double,
        close: Double,
        high: Double,
        low: Double,
        volume: Double,
    ): CandleReceived {
        val time = Instant.EPOCH.plusSeconds(index * 86400) // Daily candles
        return CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = time,
            endTime = time.plusSeconds(86400),
            openPrice = open,
            closePrice = close,
            highPrice = high,
            lowPrice = low,
            volume = volume
        )
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate with custom parameters`(numFactory: NumFactory) {
        val chaikin = ChaikinOscillatorIndicator(numFactory, shortBarCount = 5, longBarCount = 15)
        assertThat(chaikin.lag).isEqualTo(15)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(chaikin)
            .withCandlePrices(50.0, 51.0, 52.0, 53.0, 54.0)

        context.fastForward(5)
        assertThat(chaikin.value.isNaN).isFalse()
    }

    @Test
    fun `should validate constructor parameters`() {
        assertFailsWith<IllegalArgumentException> {
            ChaikinOscillatorIndicator(
                org.ta4j.core.num.DoubleNumFactory,
                shortBarCount = 0,
                longBarCount = 10
            )
        }

        assertFailsWith<IllegalArgumentException> {
            ChaikinOscillatorIndicator(
                org.ta4j.core.num.DoubleNumFactory,
                shortBarCount = 5,
                longBarCount = 0
            )
        }

        assertFailsWith<IllegalArgumentException> {
            ChaikinOscillatorIndicator(
                org.ta4j.core.num.DoubleNumFactory,
                shortBarCount = 10,
                longBarCount = 5
            )
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should track lag property correctly`(numFactory: NumFactory) {
        val chaikinDefault = ChaikinOscillatorIndicator(numFactory)
        assertThat(chaikinDefault.lag).isEqualTo(10)

        val chaikinCustom = ChaikinOscillatorIndicator(numFactory, shortBarCount = 2, longBarCount = 20)
        assertThat(chaikinCustom.lag).isEqualTo(20)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have proper toString representation`(numFactory: NumFactory) {
        val chaikin = ChaikinOscillatorIndicator(numFactory, shortBarCount = 3, longBarCount = 10)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withIndicator(chaikin)
            .withCandlePrices(50.0)

        context.advance()

        val toString = chaikin.toString()
        assertThat(toString).contains("ChaikinOscillator(3, 10)")
        assertThat(toString).contains("=>")
    }
}
