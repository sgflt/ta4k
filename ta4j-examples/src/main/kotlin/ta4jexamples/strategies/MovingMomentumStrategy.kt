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
 * Moving Momentum Strategy (migrated from Java to Kotlin).
 * 
 * This strategy uses three technical indicators to identify momentum-based entry and exit points:
 * - EMA (Exponential Moving Average) for trend identification
 * - MACD (Moving Average Convergence Divergence) for momentum confirmation  
 * - Stochastic Oscillator for overbought/oversold levels
 * 
 * Entry Rules (ALL must be true):
 * - Trend: Short EMA > Long EMA (bullish bias)
 * - Signal 1: Stochastic crosses down below 20 (oversold momentum)
 * - Signal 2: MACD > EMA(MACD) (positive momentum confirmation)
 * 
 * Exit Rules (ALL must be true):
 * - Trend: Short EMA < Long EMA (bearish bias)
 * - Signal 1: Stochastic crosses up above 80 (overbought momentum)
 * - Signal 2: MACD < EMA(MACD) (negative momentum confirmation)
 *
 * @see <a href="http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum">
 *      Moving Momentum Strategy</a>
 */
object MovingMomentumStrategy {

    @JvmStatic
    fun main(args: Array<String>) {
        
        // Getting the market data
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        
        // Building the trading strategy
        val strategyRun = MovingMomentumStrategyRun()
        
        // Running the strategy
        val backtestExecutor = BacktestExecutorBuilder().build()
            
        val statement = backtestExecutor.execute(strategyRun, marketEvents, 1000.0)
        val tradingRecord = statement.strategy.tradeRecord
        
        // Analysis (equivalent to original Java output)
        println("Number of positions for the strategy: ${tradingRecord.positionCount}")
        
        val totalReturn = ReturnCriterion()
        println("Total profit for the strategy: ${totalReturn.calculate(tradingRecord)}")
    }
}

/**
 * Strategy run configuration for Moving Momentum strategy
 */
data class MovingMomentumStrategyRun(
    override val runtimeContextFactory: RuntimeContextFactory = NOOPRuntimeContextFactory(),
    override val strategyFactory: StrategyFactory<BacktestStrategy> = 
        StrategyFactoryConverter.convert(MovingMomentumStrategyFactory()),
    override val configuration: StrategyConfiguration = StrategyConfiguration().apply {
        // EMA periods
        put(ParameterName("shortEma"), 9)
        put(ParameterName("longEma"), 26)
        
        // MACD periods
        put(ParameterName("macdFast"), 9)
        put(ParameterName("macdSlow"), 26)
        put(ParameterName("macdSignal"), 18)
        
        // Stochastic period and thresholds
        put(ParameterName("stochasticPeriod"), 14)
        put(ParameterName("stochasticLowThreshold"), 20)
        put(ParameterName("stochasticHighThreshold"), 80)
    }
) : BacktestRun

/**
 * Moving Momentum Strategy Factory using EMA, MACD, and Stochastic indicators.
 * Migrated from original Java MovingMomentumStrategy.buildStrategy() to modern Kotlin APIs.
 * 
 * Strategy logic:
 * - Entry: Short EMA > Long EMA AND Stochastic crosses down below 20 AND MACD > MACD signal
 * - Exit: Short EMA < Long EMA AND Stochastic crosses up above 80 AND MACD < MACD signal
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
        val macdFast = configuration.getInt(ParameterName("macdFast")) ?: 9
        val macdSlow = configuration.getInt(ParameterName("macdSlow")) ?: 26
        val macdSignal = configuration.getInt(ParameterName("macdSignal")) ?: 18
        val stochasticPeriod = configuration.getInt(ParameterName("stochasticPeriod")) ?: 14
        val stochasticLow = configuration.getInt(ParameterName("stochasticLowThreshold")) ?: 20
        val stochasticHigh = configuration.getInt(ParameterName("stochasticHighThreshold")) ?: 80
        
        // Create indicators (migrated from Java MovingMomentumStrategy)
        val shortEma = closePrice.ema(shortEmaPeriod)
        val longEma = closePrice.ema(longEmaPeriod)
        val macd = closePrice.macd(macdFast, macdSlow)
        val emaMacd = macd.ema(macdSignal)
        val stochasticK = Indicators.stochasticKOscillator(stochasticPeriod)
        
        // Register indicators
        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(shortEma, SHORT_EMA)
        indicatorContext.add(longEma, LONG_EMA)
        indicatorContext.add(macd, MACD)
        indicatorContext.add(emaMacd, EMA_MACD)
        indicatorContext.add(stochasticK, STOCHASTIC_K)

        return DefaultStrategy(
            name = "Moving Momentum Strategy",
            timeFrames = setOf(TimeFrame.DAY),
            entryRule = createEntryRule(indicatorContext, stochasticLow),
            exitRule = createExitRule(indicatorContext, stochasticHigh),
            indicatorContext = indicatorContext
        )
    }

    private fun createEntryRule(indicatorContext: IndicatorContext, stochasticLow: Int): Rule {
        val shortEma = indicatorContext.getNumericIndicator(SHORT_EMA)!!
        val longEma = indicatorContext.getNumericIndicator(LONG_EMA)!!
        val macd = indicatorContext.getNumericIndicator(MACD)!!
        val emaMacd = indicatorContext.getNumericIndicator(EMA_MACD)!!
        val stochasticK = indicatorContext.getNumericIndicator(STOCHASTIC_K)!!
        
        // Entry rule (migrated from Java original):
        // Trend: Short EMA > Long EMA (bullish bias)
        // Signal 1: Stochastic crosses down below 20 (oversold momentum)
        // Signal 2: MACD > EMA(MACD) (positive momentum confirmation)
        return shortEma.isGreaterThanRule(longEma)
            .and(stochasticK.crossedUnder(stochasticLow).toRule())
            .and(macd.isGreaterThanRule(emaMacd))
    }

    private fun createExitRule(indicatorContext: IndicatorContext, stochasticHigh: Int): Rule {
        val shortEma = indicatorContext.getNumericIndicator(SHORT_EMA)!!
        val longEma = indicatorContext.getNumericIndicator(LONG_EMA)!!
        val macd = indicatorContext.getNumericIndicator(MACD)!!
        val emaMacd = indicatorContext.getNumericIndicator(EMA_MACD)!!
        val stochasticK = indicatorContext.getNumericIndicator(STOCHASTIC_K)!!
        
        // Exit rule (migrated from Java original):
        // Trend: Short EMA < Long EMA (bearish bias)
        // Signal 1: Stochastic crosses up above 80 (overbought momentum)
        // Signal 2: MACD < EMA(MACD) (negative momentum confirmation)
        return shortEma.isLessThanRule(longEma)
            .and(stochasticK.crossedOver(stochasticHigh).toRule())
            .and(macd.isLessThanRule(emaMacd))
    }

    companion object {
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val SHORT_EMA = IndicatorIdentification("shortEma")
        private val LONG_EMA = IndicatorIdentification("longEma")
        private val MACD = IndicatorIdentification("macd")
        private val EMA_MACD = IndicatorIdentification("emaMacd")
        private val STOCHASTIC_K = IndicatorIdentification("stochasticK")
    }
}
