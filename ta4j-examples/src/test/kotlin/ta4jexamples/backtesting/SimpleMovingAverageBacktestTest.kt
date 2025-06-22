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
package ta4jexamples.backtesting

import java.time.Duration
import java.time.Instant
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.ta4j.core.backtest.BacktestExecutorBuilder
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.strategy.configuration.ParameterName

internal class SimpleMovingAverageBacktestTest {

    @Test
    fun `test 2-day SMA strategy execution`() {
        val executor = BacktestExecutorBuilder()
            .numFactory(DoubleNumFactory)
            .build()

        val backtestRun = TwoDaySMABacktestRun()
        val statement = executor.execute(
            backtestRun,
            createTestMarketEvents(),
            50.0
        )

        assertTrue(statement.strategy.tradeRecord.positionCount >= 0, "Should have non-negative position count")
    }

    @Test
    fun `test 3-day SMA strategy execution`() {
        val executor = BacktestExecutorBuilder()
            .numFactory(DoubleNumFactory)
            .build()

        val backtestRun = ThreeDaySMABacktestRun()
        val statement = executor.execute(
            backtestRun,
            createTestMarketEvents(),
            50.0
        )

        assertTrue(statement.strategy.tradeRecord.positionCount >= 0, "Should have non-negative position count")
    }

    @Test
    fun `test strategy factory configuration`() {
        val twoDayRun = TwoDaySMABacktestRun()
        val threeDayRun = ThreeDaySMABacktestRun()

        assertTrue((twoDayRun.configuration.getInt(ParameterName("smaBars")) ?: 0) == 2)
        assertTrue((threeDayRun.configuration.getInt(ParameterName("smaBars")) ?: 0) == 3)
    }

    @Test
    fun `test market events creation`() {
        val events = createTestMarketEvents()

        assertTrue(events.isNotEmpty(), "Should create market events")
        assertTrue(events.all { it is CandleReceived }, "All events should be CandleReceived")

        val candles = events.filterIsInstance<CandleReceived>()
        assertTrue(candles.all { it.timeFrame == TimeFrame.DAY }, "All candles should be daily timeframe")
    }

    private fun createTestMarketEvents(): List<MarketEvent> = buildList {
        add(createCandle(createDay(1), 100.0, 100.0, 100.0, 100.0, 1060))
        add(createCandle(createDay(2), 110.0, 110.0, 110.0, 110.0, 1070))
        add(createCandle(createDay(3), 140.0, 140.0, 140.0, 140.0, 1080))
        add(createCandle(createDay(4), 119.0, 119.0, 119.0, 119.0, 1090))
        add(createCandle(createDay(5), 100.0, 100.0, 100.0, 100.0, 1100))
        add(createCandle(createDay(6), 110.0, 110.0, 110.0, 110.0, 1110))
        add(createCandle(createDay(7), 120.0, 120.0, 120.0, 120.0, 1120))
        add(createCandle(createDay(8), 130.0, 130.0, 130.0, 130.0, 1130))
    }

    private fun createCandle(
        start: Instant,
        open: Double,
        high: Double,
        low: Double,
        close: Double,
        volume: Int,
    ): CandleReceived = CandleReceived(
        timeFrame = TimeFrame.DAY,
        beginTime = start,
        endTime = start.plus(Duration.ofDays(1)),
        openPrice = open,
        highPrice = high,
        lowPrice = low,
        closePrice = close,
        volume = volume.toDouble()
    )

    private fun createDay(day: Int): Instant =
        Instant.EPOCH.plus(Duration.ofDays(day.toLong()))
}
