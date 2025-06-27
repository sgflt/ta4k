/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
package ta4jexamples.strategies.ai

import java.time.Instant
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.MaximumDrawdownCriterion
import org.ta4j.core.backtest.criteria.NumberOfLosingPositionsCriterion
import org.ta4j.core.backtest.criteria.NumberOfPositionsCriterion
import org.ta4j.core.backtest.criteria.NumberOfWinningPositionsCriterion
import org.ta4j.core.backtest.criteria.pnl.GrossReturnCriterion
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.trading.live.LiveTradingBuilder

/**
 * Comprehensive example demonstrating LiveTrading with AI-powered strategy
 * and mock market data simulation using proper Ta4j criteria for P&L calculation.
 */
object AILiveTradingExample {

    private data class TradingState(
        var tradingRecord: TradingRecord,
        var barIndex: Int = 0
    )

    @JvmStatic
    fun main(args: Array<String>) {
        println("🚀 AI-Powered LiveTrading Example (Using Criteria)")
        println("=====================================")

        try {
            runTradingSimulation()
        } catch (e: Exception) {
            println("❌ Error during trading simulation: ${e.message}")
        }
    }

    private fun runTradingSimulation() {
        // Configuration
        val numFactory = DoubleNumFactory
        val timeFrame = TimeFrame.MINUTES_5
        val initialBalance = 10000.0

        // Initialize components
        val aiService = MockOpenAIService()
        val strategyFactory = AITradingStrategyFactory(aiService, timeFrame)
        val dataGenerator = MockMarketDataGenerator(timeFrame)
        val tradingState = TradingState(BackTestTradingRecord(TradeType.BUY, "AI Trading", numFactory = numFactory))

        println("🔧 Initializing LiveTrading with AI Strategy...")

        // Create indicator contexts
        val indicatorContexts = IndicatorContexts.empty().apply {
            add(IndicatorContext.empty(timeFrame))
        }

        // Build LiveTrading instance
        val liveTrading = LiveTradingBuilder()
            .withName("AI-Powered Bitcoin Trading")
            .withNumFactory(numFactory)
            .withStrategyFactory(strategyFactory)
            .withIndicatorContexts(indicatorContexts)
            .withConfiguration(StrategyConfiguration())
            .enableHistory(100) // Keep last 100 bars for indicators
            .build()

        println("✅ LiveTrading initialized successfully!")
        println("⏰ Time Frame: ${timeFrame.name}")
        println("💰 Initial Balance: $${String.format("%.2f", initialBalance)}")
        println("\n🔄 Starting market simulation...\n")

        // Generate initial candles to warm up indicators
        println("📈 Warming up indicators with historical data...")
        val warmupCandles = dataGenerator.generateCandles(25)
        warmupCandles.forEach { candle ->
            liveTrading.onCandle(candle)
            tradingState.barIndex++
        }
        println("✅ Indicators warmed up with ${warmupCandles.size} candles\n")

        // Main trading loop
        var candleCount = 0
        val maxCandles = 100

        while (candleCount < maxCandles) {
            candleCount++

            // Generate next market candle
            val candle = when {
                candleCount == 30 -> dataGenerator.simulateMarketCrash()
                candleCount == 60 -> dataGenerator.simulateMarketPump()
                else -> dataGenerator.generateNextCandle()
            }

            // Process candle through LiveTrading
            liveTrading.onCandle(candle)

            val currentPrice = candle.closePrice

            // Print market update
            println("📊 Candle #$candleCount | Price: $${String.format("%.2f", currentPrice)}")
            println("   ${dataGenerator.getMarketSummary()}")
            println(
                "   📈 OHLCV: O=${String.format("%.2f", candle.openPrice)} " +
                        "H=${String.format("%.2f", candle.highPrice)} " +
                        "L=${String.format("%.2f", candle.lowPrice)} " +
                        "C=${String.format("%.2f", currentPrice)} " +
                        "V=${String.format("%.0f", candle.volume)}"
            )

            // Check for trading signals
            val shouldEnter = liveTrading.shouldEnter()
            val shouldExit = liveTrading.shouldExit()

            // Handle entry signals
            if (shouldEnter && tradingState.tradingRecord.currentPosition.isNew) {
                val investmentAmount = initialBalance * 0.1 // 10% of balance per trade
                val entryPrice = numFactory.numOf(currentPrice)
                val shares = numFactory.numOf(investmentAmount / currentPrice) // Calculate number of shares
                val entered = tradingState.tradingRecord.enter(Instant.now(), entryPrice, shares)
                
                if (entered) {
                    println("🟢 ENTRY SIGNAL - Opening position at $${String.format("%.2f", currentPrice)}")
                    println("   💰 Investment: $${String.format("%.2f", investmentAmount)}")
                    println("   📊 Shares: ${String.format("%.6f", shares.doubleValue())}")
                    println("   📊 Bar Index: ${tradingState.barIndex}")
                }
            }

            // Handle exit signals
            if (shouldExit && tradingState.tradingRecord.currentPosition.isOpened) {
                val exitPrice = numFactory.numOf(currentPrice)
                val amount = tradingState.tradingRecord.lastEntry?.amount ?: numFactory.zero()
                val exited = tradingState.tradingRecord.exit(Instant.now(), exitPrice, amount)
                
                if (exited) {
                    val lastPosition = tradingState.tradingRecord.positions.lastOrNull()
                    val profit = lastPosition?.grossProfit?.doubleValue() ?: 0.0
                    
                    if (profit > 0) {
                        println("🟢 EXIT SIGNAL - Closing position at $${String.format("%.2f", currentPrice)} | PROFIT: $${String.format("%.2f", profit)}")
                    } else {
                        println("🔴 EXIT SIGNAL - Closing position at $${String.format("%.2f", currentPrice)} | LOSS: $${String.format("%.2f", profit)}")
                    }
                    
                    println("   📊 Bar Index: ${tradingState.barIndex}")
                }
            }
            
            // Increment bar index for proper trade tracking
            tradingState.barIndex++

            // Print AI sentiment occasionally
            if (candleCount % 10 == 0) {
                println("🧠 Market Sentiment: ${aiService.getMarketSentiment()}")
            }

            println()

            // Small delay to simulate real-time processing
            Thread.sleep(100)
        }

        // Final results
        printTradingResults(tradingState.tradingRecord, initialBalance, numFactory)
    }

    private fun printTradingResults(tradingRecord: TradingRecord, initialBalance: Double, numFactory: NumFactory) {
        println("\n🏁 TRADING SIMULATION COMPLETED")
        println("=====================================")
        println("📊 PERFORMANCE SUMMARY:")
        
        // Use Ta4j criteria for proper calculation
        val totalPositions = NumberOfPositionsCriterion().calculate(tradingRecord).intValue()
        val winningPositions = NumberOfWinningPositionsCriterion().calculate(tradingRecord).intValue()
        val losingPositions = NumberOfLosingPositionsCriterion().calculate(tradingRecord).intValue()
        val totalReturn = GrossReturnCriterion().calculate(tradingRecord).doubleValue()
        val maxDrawdown = MaximumDrawdownCriterion(numFactory).calculate(tradingRecord).doubleValue()
        
        println("   Total Trades: $totalPositions")
        println("   Winning Trades: $winningPositions")
        println("   Losing Trades: $losingPositions")

        if (totalPositions > 0) {
            val winRate = (winningPositions.toDouble() / totalPositions) * 100
            println("   Win Rate: ${String.format("%.1f", winRate)}%")
        }

        val totalPnL = (totalReturn - 1.0) * initialBalance
        println("   Total P&L: $${String.format("%.2f", totalPnL)}")
        val finalBalance = initialBalance * totalReturn
        println("   Final Balance: $${String.format("%.2f", finalBalance)}")
        val returnPercentage = (totalReturn - 1.0) * 100
        println("   Total Return: ${String.format("%.2f", returnPercentage)}%")
        println("   Max Drawdown: ${String.format("%.2f", maxDrawdown * 100)}%")

        // Performance evaluation
        println("\n📈 PERFORMANCE EVALUATION:")
        when {
            returnPercentage > 10 -> println("   🏆 EXCELLENT - Outstanding performance!")
            returnPercentage > 5 -> println("   ✅ GOOD - Solid performance")
            returnPercentage > 0 -> println("   ⚡ PROFITABLE - Positive returns")
            returnPercentage > -5 -> println("   ⚠️ MINOR LOSS - Room for improvement")
            else -> println("   ❌ SIGNIFICANT LOSS - Strategy needs revision")
        }

        if (tradingRecord.currentPosition.isOpened) {
            println("\n⚠️ OPEN POSITION DETECTED:")
            val lastEntry = tradingRecord.lastEntry
            println("   Entry Price: $${String.format("%.2f", lastEntry?.netPrice?.doubleValue() ?: 0.0)}")
            println("   Position Size: $${String.format("%.2f", lastEntry?.amount?.doubleValue() ?: 0.0)}")
        }

        println("\n🤖 AI TRADING INSIGHTS:")
        println("   • Used Ta4j criteria for accurate P&L calculation")
        println("   • TradingRecord properly tracks all positions and trades")
        println("   • ReturnCriterion calculates compound returns")
        println("   • MaximumDrawdownCriterion provides risk metrics")
        println("   • NumberOfPositionsCriterion tracks trade statistics")

        println("\n✨ Thank you for trying the AI-Powered LiveTrading Example with Criteria!")
    }
}
