/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
package ta4jexamples

import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators
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
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.trading.live.LiveTradingBuilder
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * Modern Quickstart for ta4j using LiveTrading with market events.
 *
 * Demonstrates the event-driven approach with real-time market data processing.
 */
object Quickstart {

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Ta4j Modern Live Trading Quickstart ===")
        

        // Create live trading system with our SMA crossover strategy
        val liveTrading = LiveTradingBuilder()
            .withName("BTC/USD Live Trading")
            .withStrategyFactory(SMAStrategyFactory())
            .build()

        println("Live trading system initialized!")
        println("Strategy: SMA(5) vs SMA(30) crossover with price thresholds")
        
        // Simulate receiving market events from our mock data loader
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        
        var signalCount = 0
        println("\n=== Processing Market Events ===")
        
        marketEvents.forEachIndexed { index, event ->
            // Feed the market event to our live trading system
            liveTrading.onCandle(event)
            
            // Check for trading signals after sufficient data
            if (index >= 30) { // Wait for SMA(30) to be stable
                val shouldEnter = liveTrading.shouldEnter()
                val shouldExit = liveTrading.shouldExit()
                
                if (shouldEnter || shouldExit) {
                    signalCount++
                    println("Event ${index + 1}: ${event.closePrice} - " +
                            "${if (shouldEnter) "BUY" else ""} " +
                            "${if (shouldExit) "SELL" else ""}")
                }
            }
            
            // Show progress every 10 events
            if ((index + 1) % 10 == 0) {
                println("Processed ${index + 1} market events...")
            }
        }
        
        println("\n=== Summary ===")
        println("Total events processed: ${marketEvents.size}")
        println("Trading signals generated: $signalCount")
        println("System state: ${liveTrading.lastEventTimes}")
        
        println("\nYour turn! Connect real market data feeds to this system.")
    }
}

/**
 * Strategy factory for SMA crossover with price thresholds.
 * Entry: SMA(5) crosses above SMA(30) OR price drops below $800
 * Exit: SMA(5) crosses below SMA(30) OR stop loss/gain triggers
 */
private class SMAStrategyFactory : StrategyFactory<Strategy> {
    override val tradeType: TradeType = TradeType.BUY

    override fun createStrategy(
        configuration: StrategyConfiguration,
        runtimeContext: RuntimeContext,
        indicatorContexts: IndicatorContexts,
    ): Strategy {
        val indicatorContext = indicatorContexts[TimeFrame.DAY]
        val numFactory = DoubleNumFactory

        // Create indicators using the fluent API
        val indicators = Indicators.extended(numFactory)
        val closePrice = indicators.closePrice()
        val shortSma = closePrice.sma(5)
        val longSma = closePrice.sma(30)

        // Register indicators with context
        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(shortSma, SHORT_SMA)
        indicatorContext.add(longSma, LONG_SMA)

        return DefaultStrategy(
            name = "SMA Crossover Strategy",
            timeFrames = setOf(TimeFrame.DAY),
            entryRule = createEntryRule(indicatorContext),
            exitRule = createExitRule(indicatorContext),
            indicatorContext = indicatorContext
        )
    }

    private fun createEntryRule(indicatorContext: IndicatorContext): Rule {
        val closePrice = indicatorContext.getNumericIndicator(CLOSE_PRICE)!!
        val shortSma = indicatorContext.getNumericIndicator(SHORT_SMA)!!
        val longSma = indicatorContext.getNumericIndicator(LONG_SMA)!!

        // Entry: SMA(5) > SMA(30) OR price < 800
        return shortSma.isGreaterThanRule(longSma)
            .or(closePrice.isLessThanRule(800))
    }

    private fun createExitRule(indicatorContext: IndicatorContext): Rule {
        val shortSma = indicatorContext.getNumericIndicator(SHORT_SMA)!!
        val longSma = indicatorContext.getNumericIndicator(LONG_SMA)!!

        // Exit: SMA(5) < SMA(30) OR SMA is too high (simple exit conditions)
        return shortSma.isLessThanRule(longSma)
            .or(shortSma.isGreaterThanRule(1000)) // Exit if price gets too high
    }

    companion object {
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val SHORT_SMA = IndicatorIdentification("shortSma")
        private val LONG_SMA = IndicatorIdentification("longSma")
    }
}
