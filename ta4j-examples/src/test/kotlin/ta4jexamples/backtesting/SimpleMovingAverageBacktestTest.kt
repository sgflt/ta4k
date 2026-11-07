/*
 * SPDX-License-Identifier: MIT
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
