/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.analysis

import java.text.DecimalFormat
import org.ta4j.core.backtest.BacktestExecutorBuilder
import ta4jexamples.loaders.MockMarketEventsLoader
import ta4jexamples.strategies.MovingMomentumStrategyRun

/**
 * This class demonstrates cash flow analysis of a trading strategy.
 * Migrated from CashFlowToChart.java, focusing on the analysis aspect.
 */
object CashFlowAnalysis {

    @JvmStatic
    fun main(args: Array<String>) {
        // Getting the market data
        val marketEvents = MockMarketEventsLoader.loadMarketEvents(10000)
        
        // Building the trading strategy
        val strategyRun = MovingMomentumStrategyRun()
        
        // Execute backtest
        val executor = BacktestExecutorBuilder().build()
            
        val tradingStatement = executor.execute(strategyRun, marketEvents, 1000.0)
        val tradingRecord = tradingStatement.strategy.tradeRecord
        
        // Display analysis
        val df = DecimalFormat("##.##")
        println("------------ Cash Flow Analysis ------------")
        println("Total positions: ${tradingRecord.positionCount}")
        
        // Print position details
        println("\n------------ Position Details ------------")
        var positionNumber = 1
        tradingRecord.positions.forEach { position ->
            println("\nPosition $positionNumber:")
            position.entry?.let { entry ->
                println("  Entry: Price = ${df.format(entry.netPrice.doubleValue())}, Amount = ${df.format(entry.amount.doubleValue())}")
            }
            position.exit?.let { exit ->
                println("  Exit: Price = ${df.format(exit.netPrice.doubleValue())}, Amount = ${df.format(exit.amount.doubleValue())}")
            }
            println("  Holding cost: ${df.format(position.holdingCost.doubleValue())}")
            println("  P&L: ${df.format(position.profit.doubleValue())}")
            positionNumber++
        }
        
        // Performance report
        val performanceReport = tradingStatement.performanceReport
        println("\n------------ Performance Report ------------")
        println("Total P&L: ${df.format(performanceReport.totalProfitLoss?.doubleValue() ?: 0.0)}")
        println("Total P&L %: ${df.format(performanceReport.totalProfitLossPercentage?.doubleValue() ?: 0.0)}%")
        println("Total profit: ${df.format(performanceReport.totalProfit?.doubleValue() ?: 0.0)}")
        println("Total loss: ${df.format(performanceReport.totalLoss?.doubleValue() ?: 0.0)}")
        println("Average profit: ${df.format(performanceReport.averageProfit?.doubleValue() ?: 0.0)}")
        println("Average loss: ${df.format(performanceReport.averageLoss?.doubleValue() ?: 0.0)}")
        println("Total positions: ${performanceReport.totalPositions?.intValue() ?: 0}")
        println("Maximum drawdown: ${df.format(performanceReport.maximumDrawdown?.doubleValue() ?: 0.0)}")
    }
}
