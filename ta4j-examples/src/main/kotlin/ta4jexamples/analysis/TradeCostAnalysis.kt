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
package ta4jexamples.analysis

import java.text.DecimalFormat
import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.backtest.BacktestExecutorBuilder
import org.ta4j.core.backtest.analysis.cost.LinearBorrowingCostModel
import org.ta4j.core.backtest.analysis.cost.LinearTransactionCostModel
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion
import org.ta4j.core.backtest.strategy.BacktestRun
import org.ta4j.core.backtest.strategy.BacktestStrategy
import org.ta4j.core.backtest.strategy.StrategyFactoryConverter
import org.ta4j.core.backtest.strategy.runtime.NOOPRuntimeContextFactory
import org.ta4j.core.backtest.strategy.runtime.RuntimeContextFactory
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.strategy.DefaultStrategy
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.ParameterName
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.strategy.rules.OverIndicatorRule
import org.ta4j.core.strategy.rules.UnderIndicatorRule
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * This class displays an example of the transaction cost calculation.
 */
object TradeCostAnalysis {

    @JvmStatic
    fun main(args: Array<String>) {
        // Load market events
        val marketEvents = MockMarketEventsLoader.loadMarketEvents(1000)
        
        // Create backtest run with cost models
        val backtestRun = TradeCostAnalysisRun()
        
        // Setting the trading cost models
        val feePerTrade = 0.0005
        val borrowingFee = 0.00001
        val transactionCostModel = LinearTransactionCostModel(feePerTrade)
        val borrowingCostModel = LinearBorrowingCostModel(borrowingFee)
        
        // Execute backtest
        val executor = BacktestExecutorBuilder()
            .numFactory(DoubleNumFactory)
            .transactionCostModel(transactionCostModel)
            .holdingCostModel(borrowingCostModel)
            .build()
            
        val tradingStatement = executor.execute(backtestRun, marketEvents, 1000.0)
        
        val tradingRecord = tradingStatement.strategy.tradeRecord
        
        // Display results
        val df = DecimalFormat("##.##")
        println("------------ Trading Costs Analysis ------------")
        println("Total positions: ${tradingRecord.positionCount}")
        
        var totalBorrowingCost = 0.0
        var totalTransactionCost = 0.0
        
        tradingRecord.positions.forEach { position ->
            val holdingCost = position.holdingCost.doubleValue()
            totalBorrowingCost += holdingCost
            
            position.entry?.let { entry ->
                totalTransactionCost += entry.cost.doubleValue()
            }
            position.exit?.let { exit ->
                totalTransactionCost += exit.cost.doubleValue()
            }
        }
        
        println("\n------------ Cost Summary ------------")
        println("Total borrowing costs: ${df.format(totalBorrowingCost)}")
        println("Total transaction costs: ${df.format(totalTransactionCost)}")
        println("Total costs: ${df.format(totalBorrowingCost + totalTransactionCost)}")
        
        val totalReturn = ReturnCriterion()
        println("\n------------ Performance ------------")
        println("Total return: ${df.format(totalReturn.calculate(tradingRecord).doubleValue() * 100)}%")
    }
}

/**
 * BacktestRun for trade cost analysis with short selling strategy
 */
data class TradeCostAnalysisRun(
    override val runtimeContextFactory: RuntimeContextFactory = NOOPRuntimeContextFactory(),
    override val strategyFactory: StrategyFactory<BacktestStrategy> = 
        StrategyFactoryConverter.convert(ShortSellingMomentumStrategyFactory()),
    override val configuration: StrategyConfiguration = StrategyConfiguration().apply {
        put(ParameterName("shortSmaPeriod"), 10)
        put(ParameterName("longSmaPeriod"), 50)
    }
) : BacktestRun

/**
 * Short selling momentum strategy factory
 */
private class ShortSellingMomentumStrategyFactory : StrategyFactory<Strategy> {
    override val tradeType: TradeType = TradeType.BUY // Regular strategy

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts
    ): Strategy {
        val indicatorContext = indicatorContexts[TimeFrame.DAY]
        val closePrice = Indicators.closePrice()
        
        // Configuration parameters
        val shortSmaPeriod = configuration.getInt(ParameterName("shortSmaPeriod")) ?: 10
        val longSmaPeriod = configuration.getInt(ParameterName("longSmaPeriod")) ?: 50
        
        // Create indicators
        val shortSma = closePrice.sma(shortSmaPeriod)
        val longSma = closePrice.sma(longSmaPeriod)
        
        // Register indicators
        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(shortSma, SHORT_SMA)
        indicatorContext.add(longSma, LONG_SMA)

        // Rules for momentum strategy
        val shortOverLongRule = OverIndicatorRule(shortSma, longSma)
        val shortUnderLongRule = UnderIndicatorRule(shortSma, longSma)

        return DefaultStrategy(
            name = "Momentum strategy with costs",
            timeFrames = setOf(TimeFrame.DAY),
            entryRule = shortOverLongRule,
            exitRule = shortUnderLongRule,
            indicatorContext = indicatorContext
        )
    }

    companion object {
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val SHORT_SMA = IndicatorIdentification("shortSma")
        private val LONG_SMA = IndicatorIdentification("longSma")
    }
}
