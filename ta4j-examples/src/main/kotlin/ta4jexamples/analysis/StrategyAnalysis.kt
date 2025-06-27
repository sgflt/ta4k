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

import java.time.temporal.ChronoUnit
import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
import org.ta4j.core.backtest.BacktestExecutorBuilder
import org.ta4j.core.backtest.criteria.AnalysisCriterion.PositionFilter
import org.ta4j.core.backtest.criteria.AverageReturnPerBarCriterion
import org.ta4j.core.backtest.criteria.EnterAndHoldReturnCriterion
import org.ta4j.core.backtest.criteria.LinearTransactionCostCriterion
import org.ta4j.core.backtest.criteria.MaximumDrawdownCriterion
import org.ta4j.core.backtest.criteria.NumberOfPositionsCriterion
import org.ta4j.core.backtest.criteria.PositionsRatioCriterion
import org.ta4j.core.backtest.criteria.ReturnOverMaxDrawdownCriterion
import org.ta4j.core.backtest.criteria.TimeInTradeCriterion
import org.ta4j.core.backtest.criteria.VersusEnterAndHoldCriterion
import org.ta4j.core.backtest.criteria.pnl.GrossReturnCriterion
import org.ta4j.core.backtest.strategy.BacktestRun
import org.ta4j.core.backtest.strategy.BacktestStrategy
import org.ta4j.core.backtest.strategy.StrategyFactoryConverter
import org.ta4j.core.backtest.strategy.runtime.NOOPRuntimeContextFactory
import org.ta4j.core.backtest.strategy.runtime.RuntimeContextFactory
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.strategy.DefaultStrategy
import org.ta4j.core.strategy.Rule
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.ParameterName
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * Migrated StrategyAnalysis from Java to Kotlin using modern Ta4j APIs.
 * 
 * This class displays analysis criterion values after running a trading strategy
 * over market events (equivalent to the original bar series approach).
 * 
 * Migration changes:
 * - Java BarSeries → Kotlin MarketEvents (event-driven approach)
 * - Java BarSeriesManager.run() → Kotlin BacktestExecutor.execute()
 * - Java MovingMomentumStrategy.buildStrategy() → Kotlin StrategyFactory pattern
 * - Enhanced with event-based criteria for better performance
 */
object StrategyAnalysis {

    @JvmStatic
    fun main(args: Array<String>) {
        
        // Getting the market data (modern equivalent of CsvTradesLoader.loadBitstampSeries())
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        
        // Building the trading strategy (modern equivalent of MovingMomentumStrategy.buildStrategy())
        val strategyRun = MovingMomentumStrategyRun()
        
        // Running the strategy (modern equivalent of BarSeriesManager.run())
        val backtestExecutor = BacktestExecutorBuilder().build()
            
        val statement = backtestExecutor.execute(strategyRun, marketEvents, 1000.0)
        val tradingRecord = statement.strategy.tradeRecord

        /*
         * Analysis criteria (migrated from original Java implementation)
         */

        // Total profit
        val totalReturn = GrossReturnCriterion()
        println("Total return: ${totalReturn.calculate(tradingRecord)}")
        
        // Number of bars (migrated to Time in Trade)
        println("Time in trade (days): ${TimeInTradeCriterion.days().calculate(tradingRecord)}")
        
        // Average profit (per bar)
        println("Average return (per bar): ${AverageReturnPerBarCriterion(DoubleNumFactory, ChronoUnit.DAYS).calculate(tradingRecord)}")
        
        // Number of positions
        println("Number of positions: ${NumberOfPositionsCriterion().calculate(tradingRecord)}")
        
        // Profitable position ratio
        println("Winning positions ratio: ${PositionsRatioCriterion(PositionFilter.PROFIT).calculate(tradingRecord)}")
        
        // Maximum drawdown
        println("Maximum drawdown: ${MaximumDrawdownCriterion(DoubleNumFactory).calculate(tradingRecord)}")
        
        // Reward-risk ratio
        println("Return over maximum drawdown: ${ReturnOverMaxDrawdownCriterion(DoubleNumFactory).calculate(tradingRecord)}")
        
        // Total transaction cost
        println("Total transaction cost (from \$1000): ${LinearTransactionCostCriterion(1000.0, 0.005).calculate(tradingRecord)}")
        
        // Buy-and-hold (using modern event-based approach)
        val buyAndHoldCriterion = EnterAndHoldReturnCriterion.buy(marketEvents)
        val buyAndHoldReturn = buyAndHoldCriterion.calculate(tradingRecord)
        println("Buy-and-hold return: $buyAndHoldReturn")
        
        // Total profit vs buy-and-hold (using modern event-based approach)
        val vsEnterAndHoldCriterion = VersusEnterAndHoldCriterion(marketEvents, TradeType.BUY, totalReturn)
        println("Custom strategy return vs buy-and-hold strategy return: ${vsEnterAndHoldCriterion.calculate(tradingRecord)}")
    }
}

/**
 * Strategy run configuration for Moving Momentum strategy analysis
 */
data class MovingMomentumStrategyRun(
    override val runtimeContextFactory: RuntimeContextFactory = NOOPRuntimeContextFactory(),
    override val strategyFactory: StrategyFactory<BacktestStrategy> = 
        StrategyFactoryConverter.convert(MovingMomentumStrategyFactory()),
    override val configuration: StrategyConfiguration = StrategyConfiguration().apply {
        put(ParameterName("shortEma"), 9)
        put(ParameterName("longEma"), 26)
        put(ParameterName("stochasticPeriod"), 14)
        put(ParameterName("macdShort"), 9)
        put(ParameterName("macdLong"), 26)
        put(ParameterName("macdSignal"), 18)
    }
) : BacktestRun

/**
 * Moving Momentum Strategy using MACD, EMA, and Stochastic indicators.
 * Migrated from original Java MovingMomentumStrategy to modern Kotlin APIs.
 * 
 * Strategy logic:
 * - Entry: shortEMA > longEMA AND stochastic crosses down below 20 AND MACD > MACD signal
 * - Exit: shortEMA < longEMA AND stochastic crosses up above 80 AND MACD < MACD signal
 */
private class MovingMomentumStrategyFactory : StrategyFactory<Strategy> {
    override val tradeType: TradeType = TradeType.BUY

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts
    ): Strategy {
        val indicatorContext = indicatorContexts[TimeFrame.DAY]
        val closePrice = Indicators.closePrice()
        
        // Configuration parameters
        val shortEmaPeriod = configuration.getInt(ParameterName("shortEma")) ?: 9
        val longEmaPeriod = configuration.getInt(ParameterName("longEma")) ?: 26
        val macdShort = configuration.getInt(ParameterName("macdShort")) ?: 9
        val macdLong = configuration.getInt(ParameterName("macdLong")) ?: 26
        val macdSignal = configuration.getInt(ParameterName("macdSignal")) ?: 18
        
        // Create indicators (migrated from Java MovingMomentumStrategy)
        val shortEma = closePrice.ema(shortEmaPeriod)
        val longEma = closePrice.ema(longEmaPeriod)
        val macd = closePrice.macd(macdShort, macdLong)
        val emaMacd = macd.ema(macdSignal)
        
        // Register indicators
        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(shortEma, SHORT_EMA)
        indicatorContext.add(longEma, LONG_EMA)
        indicatorContext.add(macd, MACD)
        indicatorContext.add(emaMacd, EMA_MACD)

        return DefaultStrategy(
            name = "Moving Momentum Strategy",
            timeFrames = setOf(TimeFrame.DAY),
            entryRule = createEntryRule(indicatorContext),
            exitRule = createExitRule(indicatorContext),
            indicatorContext = indicatorContext
        )
    }

    private fun createEntryRule(indicatorContext: IndicatorContext): Rule {
        val shortEma = indicatorContext.getNumericIndicator(SHORT_EMA)!!
        val longEma = indicatorContext.getNumericIndicator(LONG_EMA)!!
        val macd = indicatorContext.getNumericIndicator(MACD)!!
        val emaMacd = indicatorContext.getNumericIndicator(EMA_MACD)!!
        
        // Entry rule (simplified from Java original):
        // Trend: shortEMA > longEMA 
        // Signal: MACD > EMA(MACD)
        return shortEma.isGreaterThanRule(longEma)
            .and(macd.isGreaterThanRule(emaMacd))
    }

    private fun createExitRule(indicatorContext: IndicatorContext): Rule {
        val shortEma = indicatorContext.getNumericIndicator(SHORT_EMA)!!
        val longEma = indicatorContext.getNumericIndicator(LONG_EMA)!!
        val macd = indicatorContext.getNumericIndicator(MACD)!!
        val emaMacd = indicatorContext.getNumericIndicator(EMA_MACD)!!
        
        // Exit rule (simplified from Java original):
        // Trend: shortEMA < longEMA  
        // Signal: MACD < EMA(MACD)
        return shortEma.isLessThanRule(longEma)
            .and(macd.isLessThanRule(emaMacd))
    }

    companion object {
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val SHORT_EMA = IndicatorIdentification("shortEma")
        private val LONG_EMA = IndicatorIdentification("longEma")
        private val MACD = IndicatorIdentification("macd")
        private val EMA_MACD = IndicatorIdentification("emaMacd")
    }
}
