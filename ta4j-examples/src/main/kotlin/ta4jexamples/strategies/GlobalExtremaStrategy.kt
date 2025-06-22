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
import org.ta4j.core.indicators.numeric.helpers.HighestValueIndicator
import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory
import org.ta4j.core.strategy.DefaultStrategy
import org.ta4j.core.strategy.Rule
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.ParameterName
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * Global Extrema Strategy (migrated from Java to Kotlin).
 * 
 * Strategy which compares current price to global extrema over a week.
 * This is a mean reversion strategy that:
 * - Buys when price goes below week's low (with multiplier)
 * - Sells when price goes above week's high (with multiplier)
 * 
 * Assumes one position every 5 minutes during the whole week:
 * NB_BARS_PER_WEEK = 12 * 24 * 7 = 2016 bars
 * 
 * Entry: Close price < (week low * 1.004) - going long when price breaks below support
 * Exit: Close price > (week high * 0.996) - going short when price breaks above resistance
 */
object GlobalExtremaStrategy {

    @JvmStatic
    fun main(args: Array<String>) {
        
        // Getting the market data
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        
        // Building the trading strategy
        val strategyRun = GlobalExtremaStrategyRun()
        
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
 * Strategy run configuration for Global Extrema strategy
 */
data class GlobalExtremaStrategyRun(
    override val runtimeContextFactory: RuntimeContextFactory = NOOPRuntimeContextFactory(),
    override val strategyFactory: StrategyFactory<BacktestStrategy> = 
        StrategyFactoryConverter.convert(GlobalExtremaStrategyFactory()),
    override val configuration: StrategyConfiguration = StrategyConfiguration().apply {
        // We assume that there were at least one position every 5 minutes during the whole week
        put(ParameterName("barsPerWeek"), 12 * 24 * 7) // 2016 bars
        put(ParameterName("lowMultiplier"), 1.004) // 0.4% above low for buy threshold
        put(ParameterName("highMultiplier"), 0.996) // 0.4% below high for sell threshold
    }
) : BacktestRun

/**
 * Global Extrema Strategy Factory using highest/lowest value indicators.
 * Migrated from original Java GlobalExtremaStrategy.buildStrategy() to modern Kotlin APIs.
 * 
 * Strategy logic:
 * - Entry: Close price goes below (week low * 1.004)
 * - Exit: Close price goes above (week high * 0.996)
 */
private class GlobalExtremaStrategyFactory : StrategyFactory<Strategy> {
    override val tradeType: TradeType = TradeType.BUY

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts
    ): Strategy {
        val indicatorContext = indicatorContexts[TimeFrame.DAY]
        val closePrice = Indicators.closePrice()
        
        // Configuration parameters
        val barsPerWeek = configuration.getInt(ParameterName("barsPerWeek")) ?: (12 * 24 * 7)
        val lowMultiplier = configuration.getDouble(ParameterName("lowMultiplier")) ?: 1.004
        val highMultiplier = configuration.getDouble(ParameterName("highMultiplier")) ?: 0.996
        
        // Create price indicators (migrated from Java GlobalExtremaStrategy)
        val highPrice = Indicators.highPrice()
        val lowPrice = Indicators.lowPrice()
        
        // Getting the high/low price over the past week
        val weekHighPrice = HighestValueIndicator(defaultNumFactory, highPrice, barsPerWeek)
        val weekLowPrice = LowestValueIndicator(defaultNumFactory, lowPrice, barsPerWeek)
        
        // Apply multipliers for entry/exit thresholds
        val buyThreshold = weekLowPrice.multipliedBy(lowMultiplier)
        val sellThreshold = weekHighPrice.multipliedBy(highMultiplier)
        
        // Register indicators
        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(highPrice, HIGH_PRICE)
        indicatorContext.add(lowPrice, LOW_PRICE)
        indicatorContext.add(weekHighPrice, WEEK_HIGH)
        indicatorContext.add(weekLowPrice, WEEK_LOW)
        indicatorContext.add(buyThreshold, BUY_THRESHOLD)
        indicatorContext.add(sellThreshold, SELL_THRESHOLD)

        return DefaultStrategy(
            name = "Global Extrema Strategy",
            timeFrames = setOf(TimeFrame.DAY),
            entryRule = createEntryRule(indicatorContext),
            exitRule = createExitRule(indicatorContext),
            indicatorContext = indicatorContext
        )
    }

    private fun createEntryRule(indicatorContext: IndicatorContext): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val buyThreshold = indicatorContext.getNumericIndicator(BUY_THRESHOLD)!!
        
        // Entry rule (migrated from Java original):
        // Going long if the close price goes below the (low price * 1.004)
        return closePrice.isLessThanRule(buyThreshold)
    }

    private fun createExitRule(indicatorContext: IndicatorContext): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val sellThreshold = indicatorContext.getNumericIndicator(SELL_THRESHOLD)!!
        
        // Exit rule (migrated from Java original):
        // Going short if the close price goes above the (high price * 0.996)
        return closePrice.isGreaterThanRule(sellThreshold)
    }

    companion object {
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val HIGH_PRICE = IndicatorIdentification("highPrice")
        private val LOW_PRICE = IndicatorIdentification("lowPrice")
        private val WEEK_HIGH = IndicatorIdentification("weekHigh")
        private val WEEK_LOW = IndicatorIdentification("weekLow")
        private val BUY_THRESHOLD = IndicatorIdentification("buyThreshold")
        private val SELL_THRESHOLD = IndicatorIdentification("sellThreshold")
    }
}
