/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.strategy.rules

import java.time.Duration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.backtest.BacktestBarSeriesBuilder
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.DecimalNumFactory

class OpenedPositionMinimumBarCountRuleTest {

    private lateinit var marketContext: MarketEventTestContext
    private lateinit var tradingContext: TradingRecordTestContext

    @BeforeEach
    fun setUp() {
        // Create indicator context with 1-minute timeframe
        val minuteIndicatorContext = IndicatorContext.empty(TimeFrame.MINUTES_1)

        // Create bar series with 1-minute timeframe
        val minuteBarSeries = BacktestBarSeriesBuilder()
            .withNumFactory(DecimalNumFactory.getInstance())
            .withTimeFrame(TimeFrame.MINUTES_1)
            .withIndicatorContext(minuteIndicatorContext)
            .build()

        // Create market events with proper 1-minute timeframe
        val minuteEvents = MockMarketEventBuilder()
            .withStartTime(java.time.Instant.EPOCH)
            .withCandleDuration(Duration.ofMinutes(1))
            .withTimeFrame(TimeFrame.MINUTES_1)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0)
            .build()

        marketContext = MarketEventTestContext()
            .withNumFactory(DecimalNumFactory.getInstance())
            .withMarketEvents(minuteEvents)
            .withBarSeries(minuteBarSeries)

        tradingContext = marketContext.toTradingRecordContext()
    }

    @Test
    fun `should throw exception for negative bar count`() {
        assertThrows<IllegalArgumentException> {
            OpenedPositionMinimumBarCountRule(-1, marketContext.barSeries, tradingContext.runtimeContext)
        }
    }

    @Test
    fun `should throw exception for zero bar count`() {
        assertThrows<IllegalArgumentException> {
            OpenedPositionMinimumBarCountRule(0, marketContext.barSeries, tradingContext.runtimeContext)
        }
    }

    @Test
    fun `should return false when no position is opened`() {
        val rule = OpenedPositionMinimumBarCountRule(
            barCount = 2,
            barSeries = marketContext.barSeries,
            runtimeContext = tradingContext.runtimeContext
        )

        // No position opened - rule should return false
        assertThat(rule.isSatisfied).isFalse()
    }

    @Test
    fun `should return false immediately after opening position for 1-bar minimum`() {
        val rule = OpenedPositionMinimumBarCountRule(
            barCount = 1,
            barSeries = marketContext.barSeries,
            runtimeContext = tradingContext.runtimeContext
        )

        // Open position at first bar
        tradingContext.enter(100.0).asap()

        // Rule should return false immediately (0 bars have passed)
        assertThat(rule.isSatisfied).isFalse()
    }

    @Test
    fun `should return true after minimum bars have passed`() {
        val rule = OpenedPositionMinimumBarCountRule(
            barCount = 2,
            barSeries = marketContext.barSeries,
            runtimeContext = tradingContext.runtimeContext
        )

        // Open position at first bar
        tradingContext.enter(100.0).asap()

        // After 1 bar - should still be false (need 2 bars minimum)
        tradingContext.forwardTime(1)
        assertThat(rule.isSatisfied).isFalse()

        // After 2 bars - should now be true
        tradingContext.forwardTime(1)
        assertThat(rule.isSatisfied).isTrue()

        // After 3 bars - should still be true
        tradingContext.forwardTime(1)
        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should return false after position is closed`() {
        val rule = OpenedPositionMinimumBarCountRule(
            barCount = 1,
            barSeries = marketContext.barSeries,
            runtimeContext = tradingContext.runtimeContext
        )

        // Open position and wait for minimum bars
        tradingContext.enter(100.0).asap()
        tradingContext.forwardTime(2) // Wait 2 bars

        // Rule should be satisfied while position is open
        assertThat(rule.isSatisfied).isTrue()

        // Close position
        tradingContext.exit(100.0).asap()

        // Rule should now be false since position is closed
        assertThat(rule.isSatisfied).isFalse()
    }

    @Test
    fun `should work with different timeframes`() {
        // Test with 5-minute timeframe
        val fiveMinuteIndicatorContext = IndicatorContext.empty(TimeFrame.MINUTES_5)

        val fiveMinuteBarSeries = BacktestBarSeriesBuilder()
            .withNumFactory(DecimalNumFactory.getInstance())
            .withTimeFrame(TimeFrame.MINUTES_5)
            .withIndicatorContext(fiveMinuteIndicatorContext)
            .build()

        val fiveMinuteEvents = MockMarketEventBuilder()
            .withStartTime(java.time.Instant.EPOCH)
            .withCandleDuration(Duration.ofMinutes(5))
            .withTimeFrame(TimeFrame.MINUTES_5)
            .withCandlePrices(10.0, 11.0, 12.0, 13.0, 14.0)
            .build()

        val fiveMinuteContext = MarketEventTestContext()
            .withNumFactory(DecimalNumFactory.getInstance())
            .withMarketEvents(fiveMinuteEvents)
            .withBarSeries(fiveMinuteBarSeries)

        val fiveMinuteTradingContext = fiveMinuteContext.toTradingRecordContext()

        val rule = OpenedPositionMinimumBarCountRule(
            barCount = 3,
            barSeries = fiveMinuteContext.barSeries,
            runtimeContext = fiveMinuteTradingContext.runtimeContext
        )

        // Open position
        fiveMinuteTradingContext.enter(100.0).asap()

        // Should be false for first 2 bars
        assertThat(rule.isSatisfied).isFalse()
        fiveMinuteTradingContext.forwardTime(1)
        assertThat(rule.isSatisfied).isFalse()
        fiveMinuteTradingContext.forwardTime(1)
        assertThat(rule.isSatisfied).isFalse()

        // Should be true after 3rd bar
        fiveMinuteTradingContext.forwardTime(1)
        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should handle rapid position opening and closing`() {
        val rule = OpenedPositionMinimumBarCountRule(
            barCount = 2,
            barSeries = marketContext.barSeries,
            runtimeContext = tradingContext.runtimeContext
        )

        // Open first position
        tradingContext.enter(100.0).asap()
        assertThat(rule.isSatisfied).isFalse()

        // Close position quickly (before minimum bars)
        tradingContext.exit(100.0).after(1)
        assertThat(rule.isSatisfied).isFalse()

        // Open new position
        tradingContext.enter(100.0).asap()
        assertThat(rule.isSatisfied).isFalse()

        // Wait for minimum bars on new position
        tradingContext.forwardTime(2)
        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should provide meaningful toString representation`() {
        val rule = OpenedPositionMinimumBarCountRule(
            barCount = 5,
            barSeries = marketContext.barSeries,
            runtimeContext = tradingContext.runtimeContext
        )

        val stringRepresentation = rule.toString()
        assertThat(stringRepresentation)
            .contains("OpenedPositionMinimumBarCountRule")
            .contains("barCount=5")
    }

    @Test
    fun `should work correctly with large bar counts`() {
        val rule = OpenedPositionMinimumBarCountRule(
            barCount = 10,
            barSeries = marketContext.barSeries,
            runtimeContext = tradingContext.runtimeContext
        )

        // Open position
        tradingContext.enter(100.0).asap()

        // Should be false for 9 bars
        for (i in 1..9) {
            assertThat(rule.isSatisfied).isFalse()
            tradingContext.forwardTime(1)
        }

        // Forward to the 10th bar and should be true
        tradingContext.forwardTime(1)
        assertThat(rule.isSatisfied).isTrue()
    }
}
