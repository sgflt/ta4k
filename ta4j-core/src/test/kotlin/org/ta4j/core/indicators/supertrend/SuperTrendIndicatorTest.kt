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
package org.ta4j.core.indicators.supertrend

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.numeric.supertrend.SuperTrendIndicator
import org.ta4j.core.num.NumFactory

class SuperTrendIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate SuperTrend with exact test data from original implementation`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(createOriginalTestData())

        val superTrend = SuperTrendIndicator(numFactory)
        context.withIndicator(superTrend)

        context.fastForwardUntilStable()
        // Original test: getValue(9) -> advance to 10th bar (index 9)
        context.assertCurrent(17.602360938100002)

        context.fastForward(5) // advance 5 more bars (total 15)
        context.assertCurrent(22.78938583966133)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `regression test - should handle close price equal to previous SuperTrend value`(numFactory: NumFactory) {
        // This test reproduces the bug: https://github.com/ta4j/ta4j/issues/1120
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(createOriginalTestData())

        val superTrend = SuperTrendIndicator(numFactory)
        context.withIndicator(superTrend)

        // Test the regression: values at indices 14, 15, 16, 17, 18 should all be the same
        val expectedValue = 22.78938583966133

        // Advance to index 14 (15th bar)
        context.fastForward(15)
        context.assertCurrent(expectedValue)

        // Advance to index 15 (16th bar) - should maintain same value
        context.fastForward(1)
        context.assertCurrent(expectedValue)

        // Advance to index 16 (17th bar) - should maintain same value
        context.fastForward(1)
        context.assertCurrent(expectedValue)

        // Advance to index 17 (18th bar) - should maintain same value
        context.fastForward(1)
        context.assertCurrent(expectedValue)

        // Advance to index 18 (19th bar) - should maintain same value
        context.fastForward(1)
        context.assertCurrent(expectedValue)
    }

    /**
     * Creates the exact same test data as the original test using OHLC values.
     */
    private fun createOriginalTestData(): List<CandleReceived> {
        val testData = listOf(
            // (open, high, low, close)
            arrayOf(23.17, 23.39, 21.35, 21.48),
            arrayOf(21.25, 21.29, 20.07, 19.94),
            arrayOf(20.08, 24.30, 20.01, 21.97),
            arrayOf(22.17, 22.64, 20.78, 20.87),
            arrayOf(21.67, 22.80, 21.59, 21.65),
            arrayOf(21.47, 22.26, 20.96, 22.14),
            arrayOf(22.25, 22.31, 21.36, 21.44),
            arrayOf(21.83, 22.40, 21.59, 21.67),
            arrayOf(23.09, 23.76, 22.73, 22.90),
            arrayOf(22.93, 23.27, 21.94, 22.01),
            arrayOf(19.89, 20.47, 18.91, 19.20),
            arrayOf(21.56, 21.80, 18.83, 18.83),
            arrayOf(19.00, 19.41, 18.01, 18.35),
            arrayOf(19.89, 20.22, 6.21, 6.36),
            arrayOf(19.28, 20.58, 10.11, 10.34),
            // Edge case: close price equals previous SuperTrend value
            arrayOf(19.28, 23.58, 10.11, 22.78938583966133),
            arrayOf(19.28, 20.58, 10.11, 10.34),
            arrayOf(10.34, 12.80, 8.83, 9.83),
            arrayOf(11.83, 11.41, 5.01, 7.35)
        )

        return testData.mapIndexed { index, ohlc ->
            val startTime = Instant.EPOCH.plus(Duration.ofDays(index.toLong()))
            val endTime = startTime.plus(Duration.ofDays(1))

            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = startTime,
                endTime = endTime,
                openPrice = ohlc[0],
                highPrice = ohlc[1],
                lowPrice = ohlc[2],
                closePrice = ohlc[3],
                volume = 1000.0
            )
        }
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct string representation`(numFactory: NumFactory) {
        val superTrend1 = SuperTrendIndicator(numFactory)
        assertThat(superTrend1.toString()).contains("SuperTrend(10, 3.0)")

        val superTrend2 = SuperTrendIndicator(numFactory, barCount = 14, multiplier = 2.5)
        assertThat(superTrend2.toString()).contains("SuperTrend(14, 2.5)")
    }
}
