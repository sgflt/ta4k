/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.strategies.sma

import java.time.Instant
import org.ta4j.core.TradeType
import org.ta4j.core.api.series.Symbol
import org.ta4j.core.backtest.criteria.NumberOfPositionsCriterion
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactoryProvider
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.trading.live.LiveTradingBuilder

object LiveTradingExample {

    @JvmStatic
    fun main(args: Array<String>) {
        val timeFrame = TimeFrame.MINUTES_5
        val initialBalance = 10000.0

        // Initialization
        val dataGenerator = SimpleMarketDataGenerator(timeFrame)
        val nf = NumFactoryProvider.defaultNumFactory
        val tradingRecord =
            BackTestTradingRecord(TradeType.BUY, "Live Trading", numFactory = nf)

        val liveTrading = LiveTradingBuilder()
            .withSymbol(Symbol("Live Trading Example"))
            .withStrategyFactory(SMAStrategyFactory(timeFrame))
            .withConfiguration(StrategyConfiguration())
            .build()

        // Warm up indicators
        val warmupCandles = dataGenerator.generateCandles(25)
        warmupCandles.forEach { liveTrading.onCandle(it) }

        // Event loop
        repeat(100) { _ ->
            val candle = dataGenerator.generateNextCandle()
            liveTrading.onCandle(candle)

            val currentPrice = candle.closePrice
            val shouldEnter = liveTrading.shouldEnter()
            val shouldExit = liveTrading.shouldExit()

            if (shouldEnter && tradingRecord.currentPosition.isNew) {
                val investmentAmount = initialBalance * 0.1
                val entryPrice = candle.closePrice
                val shares = investmentAmount / currentPrice
                tradingRecord.enter(Instant.now(), nf.numOf(entryPrice), nf.numOf(shares))
                println("Entry at $currentPrice")
            }

            if (shouldExit && tradingRecord.currentPosition.isOpened) {
                val exitPrice = candle.closePrice
                val amount = tradingRecord.lastEntry!!.amount
                tradingRecord.exit(Instant.now(), nf.numOf(exitPrice), amount)
                println("Exit at $currentPrice")
            }
        }

        // Result evaluation
        val totalPositions = NumberOfPositionsCriterion().calculate(tradingRecord).intValue()
        val totalReturn = ReturnCriterion().calculate(tradingRecord).doubleValue()
        val finalBalance = initialBalance * totalReturn
        val returnPercentage = (totalReturn - 1.0) * 100

        println("\nResults:")
        println("Total trades: $totalPositions")
        println("Final balance: ${"%.2f".format(finalBalance)}")
        println("Return: ${"%.2f".format(returnPercentage)}%")
    }
}
