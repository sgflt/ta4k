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
package org.ta4j.core.indicators.numeric.momentum

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TestUtils.GENERAL_OFFSET
import org.ta4j.core.XlsTestsUtils
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory

class ATRIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate ATR with manual OHLC values`(numFactory: NumFactory) {
        val atr = Indicators.extended(numFactory).atr(3)

        // Create market events with specific OHLC values
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH,
                endTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                openPrice = 0.0,
                highPrice = 15.0,
                lowPrice = 8.0,
                closePrice = 12.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(1)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                openPrice = 0.0,
                highPrice = 11.0,
                lowPrice = 6.0,
                closePrice = 8.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(2)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(3)),
                openPrice = 0.0,
                highPrice = 17.0,
                lowPrice = 14.0,
                closePrice = 15.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(3)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(4)),
                openPrice = 0.0,
                highPrice = 17.0,
                lowPrice = 14.0,
                closePrice = 15.0,
                volume = 0.0
            ),
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.EPOCH.plus(Duration.ofDays(4)),
                endTime = Instant.EPOCH.plus(Duration.ofDays(5)),
                openPrice = 0.0,
                highPrice = 0.0,
                lowPrice = 2.0,
                closePrice = 0.0,
                volume = 0.0
            )
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicator(atr)

        // First bar: TR = high - low = 15 - 8 = 7
        context.assertNext(7.0)

        // Second bar: TR = max(11-6, |11-12|, |6-12|) = max(5, 1, 6) = 6
        // ATR = 6/3 + (1-1/3) * 7 = 2 + 2/3 * 7 = 2 + 4.667 = 6.667
        val previousAtr = 7.0
        val expectedAtr2 = 6.0 / 3 + (1 - 1.0 / 3) * previousAtr
        context.assertNext(expectedAtr2)

        // Third bar: TR = max(17-14, |17-8|, |14-8|) = max(3, 9, 6) = 9
        // ATR = 9/3 + (1-1/3) * previousAtr
        val previousAtr2 = context.firstNumericIndicator!!.value.doubleValue()
        val expectedAtr3 = 9.0 / 3 + (1 - 1.0 / 3) * previousAtr2
        context.assertNext(expectedAtr3)

        // Fourth bar: TR = max(17-14, |17-15|, |14-15|) = max(3, 2, 1) = 3
        val previousAtr3 = context.firstNumericIndicator!!.value.doubleValue()
        val expectedAtr4 = 3.0 / 3 + (1 - 1.0 / 3) * previousAtr3
        context.assertNext(expectedAtr4)

        // Fifth bar: TR = max(0-2, |0-15|, |2-15|) = max(-2, 15, 13) = 15
        // Note: negative values in max are ignored, so max(0, 15, 13) = 15
        val previousAtr4 = context.firstNumericIndicator!!.value.doubleValue()
        val expectedAtr5 = 15.0 / 3 + (1 - 1.0 / 3) * previousAtr4
        context.assertNext(expectedAtr5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate ATR period 1 from XLS data`(numFactory: NumFactory) {
        val marketEvents = XlsTestsUtils.getMarketEvents(this::class.java, "ATR.xls")

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(Indicators.extended(numFactory).atr(1))

        // Process all events
        while (context.advance()) {
            // Continue processing
        }

        // Verify final value matches XLS
        assertThat(context.firstNumericIndicator!!.value.doubleValue())
            .isCloseTo(4.8, Offset.offset(GENERAL_OFFSET))

        // Verify all values match expected from XLS by recreating context
        val expectedIndicator = XlsTestsUtils.getIndicator(this::class.java, "ATR.xls", 6, numFactory)
        val atr = Indicators.extended(numFactory).atr(1)
        val verificationContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(atr, "atr")
            .withIndicator(expectedIndicator, "expected")

        verificationContext.assertIndicatorEquals(expectedIndicator, atr)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate ATR period 13 from XLS data`(numFactory: NumFactory) {
        val marketEvents = XlsTestsUtils.getMarketEvents(this::class.java, "ATR.xls")
        val expectedIndicator = XlsTestsUtils.getIndicator(this::class.java, "ATR.xls", 7, numFactory)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(Indicators.extended(numFactory).atr(13))

        // Process all events
        while (context.advance()) {
            // Continue processing
        }

        // Verify final value matches XLS
        assertThat(context.firstNumericIndicator!!.value.doubleValue())
            .isCloseTo(8.8082, Offset.offset(GENERAL_OFFSET))

        // Verify all values match expected from XLS
        val atr = Indicators.extended(numFactory).atr(13)
        val verificationContext = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)
            .withIndicator(atr)
            .withIndicator(expectedIndicator, "expected")

        verificationContext.assertIndicatorEquals(expectedIndicator, atr)
    }

    @Test
    fun `should handle invalid parameters gracefully`() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            Indicators.atr(-1)
        }

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            Indicators.atr(0)
        }
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should be stable after sufficient bars`(numFactory: NumFactory) {
        val barCount = 5
        val atr = Indicators.extended(numFactory).atr(barCount)

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0)
            .withIndicator(atr)

        // Should not be stable initially
        context.fastForward(barCount - 1)
        assertThat(atr.isStable).isFalse()

        // Should be stable after barCount bars
        context.fastForward(1)
        assertThat(atr.isStable).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should have correct lag value`(numFactory: NumFactory) {
        val barCount = 14
        val atr = Indicators.extended(numFactory).atr(barCount)

        assertThat(atr.lag).isEqualTo(barCount)
    }
}
