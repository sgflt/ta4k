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
 * ADX indicator based strategy (migrated from Java to Kotlin).
 * 
 * The Average Directional Index (ADX) strategy uses:
 * - ADX indicator to measure trend strength
 * - +DI and -DI indicators for trend direction
 * - SMA for price trend confirmation
 * 
 * Entry: ADX > 20 AND +DI crosses above -DI AND close > SMA
 * Exit: ADX > 20 AND +DI crosses below -DI AND close < SMA
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx">
 *      ADX Technical Analysis</a>
 */
object ADXStrategy {

    @JvmStatic
    fun main(args: Array<String>) {
        
        // Getting the market data
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        
        // Building the trading strategy
        val strategyRun = ADXStrategyRun()
        
        // Running the strategy
        val backtestExecutor = BacktestExecutorBuilder()
            .numFactory(DoubleNumFactory)
            .build()
            
        val statement = backtestExecutor.execute(strategyRun, marketEvents, 1000.0)
        val tradingRecord = statement.strategy.tradeRecord
        
        println("Number of positions for the strategy: ${tradingRecord.positionCount}")
        
        val totalReturn = ReturnCriterion()
        println("Total return for the strategy: ${totalReturn.calculate(tradingRecord)}")
    }
}

/**
 * Strategy run configuration for ADX strategy
 */
data class ADXStrategyRun(
    override val runtimeContextFactory: RuntimeContextFactory = NOOPRuntimeContextFactory(),
    override val strategyFactory: StrategyFactory<BacktestStrategy> = 
        StrategyFactoryConverter.convert(ADXStrategyFactory()),
    override val configuration: StrategyConfiguration = StrategyConfiguration().apply {
        put(ParameterName("adxPeriod"), 14)
        put(ParameterName("smaPeriod"), 50)
        put(ParameterName("adxThreshold"), 20)
    }
) : BacktestRun

/**
 * ADX Strategy Factory using ADX, +DI, -DI, and SMA indicators.
 * Migrated from original Java ADXStrategy.buildStrategy() to modern Kotlin APIs.
 * 
 * Strategy logic:
 * - Entry: ADX > threshold AND +DI crosses above -DI AND close > SMA
 * - Exit: ADX > threshold AND +DI crosses below -DI AND close < SMA
 */
private class ADXStrategyFactory : StrategyFactory<Strategy> {
    override val tradeType: TradeType = TradeType.BUY

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts
    ): Strategy {
        val indicatorContext = indicatorContexts[TimeFrame.DAY]
        val closePrice = Indicators.closePrice()
        
        // Configuration parameters
        val adxPeriod = configuration.getInt(ParameterName("adxPeriod")) ?: 14
        val smaPeriod = configuration.getInt(ParameterName("smaPeriod")) ?: 50
        val adxThreshold = configuration.getInt(ParameterName("adxThreshold")) ?: 20
        
        // Create indicators (migrated from Java ADXStrategy)
        val sma = closePrice.sma(smaPeriod)
        val adx = Indicators.adx(adxPeriod)
        val plusDI = Indicators.plusDII(adxPeriod)
        val minusDI = Indicators.minusDII(adxPeriod)
        
        // Register indicators
        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(sma, SMA)
        indicatorContext.add(adx, ADX)
        indicatorContext.add(plusDI, PLUS_DI)
        indicatorContext.add(minusDI, MINUS_DI)

        return DefaultStrategy(
            name = "ADX Strategy",
            timeFrames = setOf(TimeFrame.DAY),
            entryRule = createEntryRule(indicatorContext, adxThreshold),
            exitRule = createExitRule(indicatorContext, adxThreshold),
            indicatorContext = indicatorContext
        )
    }

    private fun createEntryRule(indicatorContext: IndicatorContext, adxThreshold: Int): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val sma = indicatorContext.getNumericIndicator(SMA)!!
        val adx = indicatorContext.getNumericIndicator(ADX)!!
        val plusDI = indicatorContext.getNumericIndicator(PLUS_DI)!!
        val minusDI = indicatorContext.getNumericIndicator(MINUS_DI)!!
        
        // Entry rule (migrated from Java original):
        // ADX > 20 AND +DI crosses above -DI AND close > SMA
        return adx.isGreaterThanRule(adxThreshold)
            .and(plusDI.crossedOver(minusDI).toRule())
            .and(closePrice.isGreaterThanRule(sma))
    }

    private fun createExitRule(indicatorContext: IndicatorContext, adxThreshold: Int): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val sma = indicatorContext.getNumericIndicator(SMA)!!
        val adx = indicatorContext.getNumericIndicator(ADX)!!
        val plusDI = indicatorContext.getNumericIndicator(PLUS_DI)!!
        val minusDI = indicatorContext.getNumericIndicator(MINUS_DI)!!
        
        // Exit rule (migrated from Java original):
        // ADX > 20 AND +DI crosses below -DI AND close < SMA
        return adx.isGreaterThanRule(adxThreshold)
            .and(plusDI.crossedUnder(minusDI).toRule())
            .and(closePrice.isLessThanRule(sma))
    }

    companion object {
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val SMA = IndicatorIdentification("sma")
        private val ADX = IndicatorIdentification("adx")
        private val PLUS_DI = IndicatorIdentification("plusDI")
        private val MINUS_DI = IndicatorIdentification("minusDI")
    }
}
