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
 * CCI Correction Strategy (migrated from Java to Kotlin).
 * 
 * The Commodity Channel Index (CCI) Correction strategy uses:
 * - Long CCI (200-period) to identify the overall trend  
 * - Short CCI (5-period) for entry/exit signals
 * - Standard CCI thresholds of +100/-100
 * - Unstable bars: 5
 * 
 * Entry: Long CCI > +100 (bull trend) AND Short CCI < -100 (oversold signal)
 * Exit: Long CCI < -100 (bear trend) AND Short CCI > +100 (overbought signal)
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:cci_correction">
 *      CCI Correction Strategy</a>
 */
object CCICorrectionStrategy {

    @JvmStatic
    fun main(args: Array<String>) {
        
        // Getting the market data (modern equivalent of CsvTradesLoader.loadBitstampSeries())
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        
        // Building the trading strategy (modern equivalent of buildStrategy(series))
        val strategyRun = CCICorrectionStrategyRun()
        
        // Running the strategy (modern equivalent of BarSeriesManager.run())
        val backtestExecutor = BacktestExecutorBuilder()
            .numFactory(DoubleNumFactory)
            .build()
            
        val statement = backtestExecutor.execute(strategyRun, marketEvents, 1000.0)
        val tradingRecord = statement.strategy.tradeRecord
        
        // Analysis (equivalent to original Java output)
        println("Number of positions for the strategy: ${tradingRecord.positionCount}")
        
        val totalReturn = ReturnCriterion()
        println("Total return for the strategy: ${totalReturn.calculate(tradingRecord)}")
    }
}

/**
 * Strategy run configuration for CCI Correction strategy
 */
data class CCICorrectionStrategyRun(
    override val runtimeContextFactory: RuntimeContextFactory = NOOPRuntimeContextFactory(),
    override val strategyFactory: StrategyFactory<BacktestStrategy> = 
        StrategyFactoryConverter.convert(CCICorrectionStrategyFactory()),
    override val configuration: StrategyConfiguration = StrategyConfiguration().apply {
        put(ParameterName("longCciPeriod"), 200)
        put(ParameterName("shortCciPeriod"), 5)
        put(ParameterName("upperThreshold"), 100)
        put(ParameterName("lowerThreshold"), -100)
    }
) : BacktestRun

/**
 * CCI Correction Strategy Factory using long and short CCI indicators.
 * Migrated from original Java CCICorrectionStrategy.buildStrategy() to modern Kotlin APIs.
 * 
 * Strategy logic:
 * - Entry: Long CCI > +100 AND Short CCI < -100
 * - Exit: Long CCI < -100 AND Short CCI > +100
 */
private class CCICorrectionStrategyFactory : StrategyFactory<Strategy> {
    override val tradeType: TradeType = TradeType.BUY

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts
    ): Strategy {
        val indicatorContext = indicatorContexts[TimeFrame.DAY]
        
        // Configuration parameters
        val longCciPeriod = configuration.getInt(ParameterName("longCciPeriod")) ?: 200
        val shortCciPeriod = configuration.getInt(ParameterName("shortCciPeriod")) ?: 5
        val upperThreshold = configuration.getInt(ParameterName("upperThreshold")) ?: 100
        val lowerThreshold = configuration.getInt(ParameterName("lowerThreshold")) ?: -100

        // Create CCI indicators (migrated from Java CCICorrectionStrategy)
        val longCci = Indicators.cci(longCciPeriod)
        val shortCci = Indicators.cci(shortCciPeriod)
        
        // Register indicators
        indicatorContext.add(longCci, LONG_CCI)
        indicatorContext.add(shortCci, SHORT_CCI)

        return DefaultStrategy(
            name = "CCI Correction Strategy",
            timeFrames = setOf(TimeFrame.DAY),
            entryRule = createEntryRule(indicatorContext, upperThreshold, lowerThreshold),
            exitRule = createExitRule(indicatorContext, upperThreshold, lowerThreshold),
            indicatorContext = indicatorContext
        )
    }

    private fun createEntryRule(
        indicatorContext: IndicatorContext, 
        upperThreshold: Int, 
        lowerThreshold: Int
    ): Rule {
        val longCci = indicatorContext.getNumericIndicator(LONG_CCI)!!
        val shortCci = indicatorContext.getNumericIndicator(SHORT_CCI)!!
        
        // Entry rule (migrated from Java original):
        // Long CCI > +100 (bull trend) AND Short CCI < -100 (oversold signal)
        return longCci.isGreaterThanRule(upperThreshold)
            .and(shortCci.isLessThanRule(lowerThreshold))
    }

    private fun createExitRule(
        indicatorContext: IndicatorContext, 
        upperThreshold: Int, 
        lowerThreshold: Int
    ): Rule {
        val longCci = indicatorContext.getNumericIndicator(LONG_CCI)!!
        val shortCci = indicatorContext.getNumericIndicator(SHORT_CCI)!!
        
        // Exit rule (migrated from Java original):
        // Long CCI < -100 (bear trend) AND Short CCI > +100 (overbought signal)
        return longCci.isLessThanRule(lowerThreshold)
            .and(shortCci.isGreaterThanRule(upperThreshold))
    }

    companion object {
        private val LONG_CCI = IndicatorIdentification("longCci")
        private val SHORT_CCI = IndicatorIdentification("shortCci")
    }
}
