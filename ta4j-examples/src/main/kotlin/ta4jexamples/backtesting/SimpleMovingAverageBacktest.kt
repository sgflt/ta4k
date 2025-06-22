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
import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.backtest.BacktestExecutorBuilder
import org.ta4j.core.backtest.strategy.BacktestRun
import org.ta4j.core.backtest.strategy.BacktestStrategy
import org.ta4j.core.backtest.strategy.StrategyFactoryConverter
import org.ta4j.core.backtest.strategy.runtime.NOOPRuntimeContextFactory
import org.ta4j.core.backtest.strategy.runtime.RuntimeContextFactory
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.strategy.DefaultStrategy
import org.ta4j.core.strategy.Rule
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.ParameterName
import org.ta4j.core.strategy.configuration.StrategyConfiguration

object SimpleMovingAverageBacktest {

    @JvmStatic
    fun main(args: Array<String>) {
        val backtestExecutor = BacktestExecutorBuilder().build()

        println("=== Simple Moving Average Backtest ===")

        // Test 2-day SMA strategy
        val twoDay = TwoDaySMABacktestRun()
        val twoDayStatement = backtestExecutor.execute(
            twoDay,
            createCandleEvents(),
            50.0
        )

        // Test 3-day SMA strategy
        val threeDay = ThreeDaySMABacktestRun()
        val threeDayStatement = backtestExecutor.execute(
            threeDay,
            createCandleEvents(),
            50.0
        )

        println("\nDetailed Statements:")
        println("2-Day: $twoDayStatement")
        println("3-Day: $threeDayStatement")
    }

    private fun createCandleEvents(): List<MarketEvent> = buildList {
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

private val SMA_BARS = ParameterName("smaBars")

data class TwoDaySMABacktestRun(
    override val runtimeContextFactory: RuntimeContextFactory = NOOPRuntimeContextFactory(),
    override val strategyFactory: StrategyFactory<BacktestStrategy> =
        StrategyFactoryConverter.convert(TwoDaySMAStrategyFactory()),
    override val configuration: StrategyConfiguration = StrategyConfiguration().apply {
        put(SMA_BARS, 2)
    },
) : BacktestRun

data class ThreeDaySMABacktestRun(
    override val runtimeContextFactory: RuntimeContextFactory = NOOPRuntimeContextFactory(),
    override val strategyFactory: StrategyFactory<BacktestStrategy> =
        StrategyFactoryConverter.convert(ThreeDaySMAStrategyFactory()),
    override val configuration: StrategyConfiguration = StrategyConfiguration().apply {
        put(SMA_BARS, 3)
    },
) : BacktestRun

private class TwoDaySMAStrategyFactory : StrategyFactory<Strategy> {
    override val tradeType: TradeType = TradeType.BUY

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts,
    ): Strategy {
        val indicatorContext = indicatorContexts[TimeFrame.DAY]
        val closePrice = Indicators.closePrice()
        val sma = closePrice.sma(configuration.getInt(SMA_BARS) ?: error("Missing configuration of SMA_BARS"))

        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(sma, SMA)

        return DefaultStrategy(
            name = "2-Day SMA Strategy",
            timeFrames = setOf(TimeFrame.DAY),
            entryRule = createEntryRule(indicatorContext),
            exitRule = createExitRule(indicatorContext),
            indicatorContext = indicatorContext
        )
    }

    private fun createEntryRule(indicatorContext: IndicatorContext): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val sma = indicatorContext.getNumericIndicator(SMA)!!
        return closePrice.isGreaterThanRule(sma)
    }

    private fun createExitRule(indicatorContext: IndicatorContext): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val sma = indicatorContext.getNumericIndicator(SMA)!!
        return closePrice.isLessThanRule(sma)
    }

    companion object {
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val SMA = IndicatorIdentification("sma")
    }
}

private class ThreeDaySMAStrategyFactory : StrategyFactory<Strategy> {
    override val tradeType: TradeType = TradeType.BUY

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts,
    ): Strategy {
        val indicatorContext = indicatorContexts[TimeFrame.DAY]
        val closePrice = Indicators.closePrice()
        val sma = closePrice.sma(configuration.getInt(SMA_BARS) ?: error("Missing configuration of SMA_BARS"))

        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(sma, SMA)

        return DefaultStrategy(
            name = "3-Day SMA Strategy",
            timeFrames = setOf(TimeFrame.DAY),
            entryRule = createEntryRule(indicatorContext),
            exitRule = createExitRule(indicatorContext),
            indicatorContext = indicatorContext
        )
    }

    private fun createEntryRule(indicatorContext: IndicatorContext): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val sma = indicatorContext.getNumericIndicator(SMA)!!
        return closePrice.isGreaterThanRule(sma)
    }

    private fun createExitRule(indicatorContext: IndicatorContext): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val sma = indicatorContext.getNumericIndicator(SMA)!!
        return closePrice.isLessThanRule(sma)
    }

    companion object {
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val SMA = IndicatorIdentification("sma")
    }
}
