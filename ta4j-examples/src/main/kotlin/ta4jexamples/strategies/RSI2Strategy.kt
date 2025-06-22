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
package ta4jexamples.strategies

import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.backtest.BacktestExecutorBuilder
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion
import org.ta4j.core.backtest.strategy.BacktestRun
import org.ta4j.core.backtest.strategy.BacktestStrategy
import org.ta4j.core.backtest.strategy.StrategyFactoryConverter
import org.ta4j.core.backtest.strategy.runtime.NOOPRuntimeContextFactory
import org.ta4j.core.backtest.strategy.runtime.RuntimeContextFactory
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
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * 2-Period RSI Strategy (migrated from Java to Kotlin).
 * 
 * This is a mean-reversion strategy that uses a 2-period RSI to identify 
 * short-term oversold/overbought conditions within a longer-term trend.
 * 
 * The strategy uses:
 * - 2-period RSI for entry/exit signals (very sensitive to price changes)
 * - 5-period SMA for short-term trend
 * - 200-period SMA for long-term trend filter
 * 
 * Entry Rules (ALL must be true):
 * - Trend: 5-period SMA > 200-period SMA (long-term uptrend)
 * - Signal 1: RSI crosses down below 5 (extremely oversold)
 * - Signal 2: Close price < 5-period SMA (pullback confirmation)
 * 
 * Exit Rules (ALL must be true):
 * - Trend: 5-period SMA < 200-period SMA (long-term downtrend)
 * - Signal 1: RSI crosses up above 95 (extremely overbought)
 * - Signal 2: Close price > 5-period SMA (breakout confirmation)
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2">
 *      2-Period RSI Strategy</a>
 */
object RSI2Strategy {

    @JvmStatic
    fun main(args: Array<String>) {
        
        // Getting the market data
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        
        // Building the trading strategy
        val strategyRun = RSI2StrategyRun()
        
        // Running the strategy
        val backtestExecutor = BacktestExecutorBuilder().build()
            
        val statement = backtestExecutor.execute(strategyRun, marketEvents, 1000.0)
        val tradingRecord = statement.strategy.tradeRecord
        
        // Analysis (equivalent to original Java output)
        println("Number of positions for the strategy: ${tradingRecord.positionCount}")
        
        val totalReturn = ReturnCriterion()
        println("Total return for the strategy: ${totalReturn.calculate(tradingRecord)}")
    }
}

/**
 * Strategy run configuration for RSI2 strategy
 */
data class RSI2StrategyRun(
    override val runtimeContextFactory: RuntimeContextFactory = NOOPRuntimeContextFactory(),
    override val strategyFactory: StrategyFactory<BacktestStrategy> = 
        StrategyFactoryConverter.convert(RSI2StrategyFactory()),
    override val configuration: StrategyConfiguration = StrategyConfiguration().apply {
        // RSI configuration
        put(ParameterName("rsiPeriod"), 2)
        put(ParameterName("rsiOversoldThreshold"), 5)
        put(ParameterName("rsiOverboughtThreshold"), 95)
        
        // SMA periods for trend filtering
        put(ParameterName("shortSmaPeriod"), 5)
        put(ParameterName("longSmaPeriod"), 200)
    }
) : BacktestRun

/**
 * RSI2 Strategy Factory using 2-period RSI and SMA trend filters.
 * Migrated from original Java RSI2Strategy.buildStrategy() to modern Kotlin APIs.
 * 
 * Strategy logic:
 * - Entry: Short SMA > Long SMA AND RSI crosses down below 5 AND Close < Short SMA
 * - Exit: Short SMA < Long SMA AND RSI crosses up above 95 AND Close > Short SMA
 */
private class RSI2StrategyFactory : StrategyFactory<Strategy> {
    override val tradeType: TradeType = TradeType.BUY

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts
    ): Strategy {
        val indicatorContext = indicatorContexts[TimeFrame.DAY]
        val closePrice = Indicators.closePrice()
        
        // Configuration parameters
        val rsiPeriod = configuration.getInt(ParameterName("rsiPeriod")) ?: 2
        val rsiOversoldThreshold = configuration.getInt(ParameterName("rsiOversoldThreshold")) ?: 5
        val rsiOverboughtThreshold = configuration.getInt(ParameterName("rsiOverboughtThreshold")) ?: 95
        val shortSmaPeriod = configuration.getInt(ParameterName("shortSmaPeriod")) ?: 5
        val longSmaPeriod = configuration.getInt(ParameterName("longSmaPeriod")) ?: 200
        
        // Create indicators (migrated from Java RSI2Strategy)
        val shortSma = closePrice.sma(shortSmaPeriod)
        val longSma = closePrice.sma(longSmaPeriod)
        val rsi = closePrice.rsi(rsiPeriod)
        
        // Register indicators
        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(shortSma, SHORT_SMA)
        indicatorContext.add(longSma, LONG_SMA)
        indicatorContext.add(rsi, RSI)

        return DefaultStrategy(
            name = "RSI2 Strategy",
            timeFrames = setOf(TimeFrame.DAY),
            entryRule = createEntryRule(indicatorContext, rsiOversoldThreshold),
            exitRule = createExitRule(indicatorContext, rsiOverboughtThreshold),
            indicatorContext = indicatorContext
        )
    }

    private fun createEntryRule(indicatorContext: IndicatorContext, rsiOversoldThreshold: Int): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val shortSma = indicatorContext.getNumericIndicator(SHORT_SMA)!!
        val longSma = indicatorContext.getNumericIndicator(LONG_SMA)!!
        val rsi = indicatorContext.getNumericIndicator(RSI)!!
        
        // Entry rule (migrated from Java original):
        // Trend: 5-period SMA > 200-period SMA (long-term uptrend)
        // Signal 1: RSI crosses down below 5 (extremely oversold)
        // Signal 2: Close price < 5-period SMA (pullback confirmation)
        return shortSma.isGreaterThanRule(longSma)
            .and(rsi.crossedUnder(rsiOversoldThreshold).toRule())
            .and(closePrice.isLessThanRule(shortSma))
    }

    private fun createExitRule(indicatorContext: IndicatorContext, rsiOverboughtThreshold: Int): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val shortSma = indicatorContext.getNumericIndicator(SHORT_SMA)!!
        val longSma = indicatorContext.getNumericIndicator(LONG_SMA)!!
        val rsi = indicatorContext.getNumericIndicator(RSI)!!
        
        // Exit rule (migrated from Java original):
        // Trend: 5-period SMA < 200-period SMA (long-term downtrend)
        // Signal 1: RSI crosses up above 95 (extremely overbought)
        // Signal 2: Close price > 5-period SMA (breakout confirmation)
        return shortSma.isLessThanRule(longSma)
            .and(rsi.crossedOver(rsiOverboughtThreshold).toRule())
            .and(closePrice.isGreaterThanRule(shortSma))
    }

    companion object {
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val SHORT_SMA = IndicatorIdentification("shortSma")
        private val LONG_SMA = IndicatorIdentification("longSma")
        private val RSI = IndicatorIdentification("rsi")
    }
}
