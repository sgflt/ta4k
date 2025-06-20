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
package ta4jexamples.strategies.sma

import java.time.Instant
import org.ta4j.core.TradeType
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
            .withName("Live Trading Example")
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
        println("Final balance: ${String.format("%.2f", finalBalance)}")
        println("Return: ${String.format("%.2f", returnPercentage)}%")
    }
}
