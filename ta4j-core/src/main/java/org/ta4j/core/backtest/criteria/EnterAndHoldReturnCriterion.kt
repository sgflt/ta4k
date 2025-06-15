/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core.backtest.criteria

import org.ta4j.core.TradeType
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.backtest.BacktestBarSeries
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Enter and hold criterion, returned in decimal format.
 *
 * Calculates the gross return (in percent) of an enter-and-hold strategy:
 *
 * - For [tradeType] = [TradeType.BUY]: Buy with the close price
 *   of the first bar and sell with the close price of the last bar.
 * - For [tradeType] = [TradeType.SELL]: Sell with the close
 *   price of the first bar and buy with the close price of the last bar.
 *
 * @see [Buy and hold](http://en.wikipedia.org/wiki/Buy_and_hold)
 */
class EnterAndHoldReturnCriterion(
    private val series: BarSeries,
    private val tradeType: TradeType
) : AnalysisCriterion {

    companion object {
        fun buy(series: BarSeries): EnterAndHoldReturnCriterion {
            return EnterAndHoldReturnCriterion(series, TradeType.BUY)
        }

        fun sell(series: BarSeries): EnterAndHoldReturnCriterion {
            return EnterAndHoldReturnCriterion(series, TradeType.SELL)
        }
    }

    override fun calculate(position: Position): Num {
        val backtestSeries = series as BacktestBarSeries
        if (backtestSeries.isEmpty || !position.isClosed) {
            return defaultNumFactory.one()
        }
        
        val firstPrice = backtestSeries.getBar(backtestSeries.beginIndex).closePrice
        val lastPrice = backtestSeries.getBar(backtestSeries.endIndex).closePrice
        
        return when (tradeType) {
            TradeType.BUY -> lastPrice / firstPrice
            TradeType.SELL -> {
                val two = firstPrice.numFactory.two()
                two - (lastPrice / firstPrice)
            }
        }
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        val backtestSeries = series as BacktestBarSeries
        if (backtestSeries.isEmpty) {
            return defaultNumFactory.one()
        }
        
        val firstPrice = backtestSeries.getBar(backtestSeries.beginIndex).closePrice
        val lastPrice = backtestSeries.getBar(backtestSeries.endIndex).closePrice
        
        return when (tradeType) {
            TradeType.BUY -> lastPrice / firstPrice
            TradeType.SELL -> {
                val two = firstPrice.numFactory.two()
                two - (lastPrice / firstPrice)
            }
        }
    }
}
