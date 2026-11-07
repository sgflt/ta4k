/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.candles

import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.DecimalNumFactory

class UpperShadowIndicatorTest {

    private lateinit var context: MarketEventTestContext

    @BeforeEach
    fun setUp() {
        context = MarketEventTestContext()
            .withNumFactory(DecimalNumFactory.getInstance())
            .withIndicator(UpperShadowIndicator(DecimalNumFactory.getInstance()))
    }

    @Test
    fun `should calculate upper shadow for bullish candles`() {
        // Create test data with known upper shadows
        // Bullish candle: open=10, close=18, high=20, low=10 -> upper shadow = high - close = 20 - 18 = 2
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.parse("2023-01-01T00:00:00Z"),
                endTime = Instant.parse("2023-01-01T23:59:59Z"),
                openPrice = 10.0,
                highPrice = 20.0,
                lowPrice = 10.0,
                closePrice = 18.0,
                volume = 1000.0
            )
        )

        context.withMarketEvents(events)
            .assertNext(2.0) // high - close = 20 - 18 = 2
    }

    @Test
    fun `should calculate upper shadow for bearish candles`() {
        // Bearish candle: open=18, close=10, high=20, low=8 -> upper shadow = high - open = 20 - 18 = 2
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.parse("2023-01-01T00:00:00Z"),
                endTime = Instant.parse("2023-01-01T23:59:59Z"),
                openPrice = 18.0,
                highPrice = 20.0,
                lowPrice = 8.0,
                closePrice = 10.0,
                volume = 1000.0
            )
        )

        context.withMarketEvents(events)
            .assertNext(2.0) // high - open = 20 - 18 = 2
    }

    @Test
    fun `should calculate zero upper shadow when high equals body top`() {
        // Bullish candle with no upper shadow: open=10, close=20, high=20, low=10
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.parse("2023-01-01T00:00:00Z"),
                endTime = Instant.parse("2023-01-01T23:59:59Z"),
                openPrice = 10.0,
                highPrice = 20.0,
                lowPrice = 10.0,
                closePrice = 20.0,
                volume = 1000.0
            )
        )

        context.withMarketEvents(events)
            .assertNext(0.0) // high - close = 20 - 20 = 0
    }

    @Test
    fun `should handle doji candles correctly`() {
        // Doji candle: open=15, close=15, high=16, low=14 -> upper shadow = high - max(open,close) = 16 - 15 = 1
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.parse("2023-01-01T00:00:00Z"),
                endTime = Instant.parse("2023-01-01T23:59:59Z"),
                openPrice = 15.0,
                highPrice = 16.0,
                lowPrice = 14.0,
                closePrice = 15.0,
                volume = 1000.0
            )
        )

        context.withMarketEvents(events)
            .assertNext(1.0) // high - max(open,close) = 16 - 15 = 1
    }

    @Test
    fun `should calculate upper shadows for multiple candles`() {
        val events = MockMarketEventBuilder()
            .withCandleDuration(java.time.Duration.ofDays(1))
            .withStartTime(Instant.parse("2023-01-01T00:00:00Z"))
            .candle().openPrice(10.0).highPrice(20.0).lowPrice(10.0).closePrice(18.0).add()
            .candle().openPrice(17.0).highPrice(21.0).lowPrice(11.0).closePrice(12.0).add()
            .candle().openPrice(15.0).highPrice(16.0).lowPrice(14.0).closePrice(15.0).add()
            .candle().openPrice(15.0).highPrice(15.0).lowPrice(8.0).closePrice(11.0).add()
            .build()

        context.withMarketEvents(events)
            .assertNext(2.0)  // First candle: high - close = 20 - 18 = 2
            .assertNext(4.0)  // Second candle: high - open = 21 - 17 = 4  
            .assertNext(1.0)  // Third candle: high - close = 16 - 15 = 1
            .assertNext(0.0)  // Fourth candle: high - open = 15 - 15 = 0
    }

    @Test
    fun `should be stable immediately for real-time candles`() {
        val events = listOf(
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = Instant.parse("2023-01-01T00:00:00Z"),
                endTime = Instant.parse("2023-01-01T23:59:59Z"),
                openPrice = 10.0,
                highPrice = 20.0,
                lowPrice = 10.0,
                closePrice = 18.0,
                volume = 1000.0
            )
        )

        context.withMarketEvents(events)
            .fastForward(1)
            .assertIsStable()
    }
}
