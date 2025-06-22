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
package org.ta4j.core.backtest.criteria

import org.ta4j.core.TradeType
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
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
    private val marketEvents: List<MarketEvent>,
    private val tradeType: TradeType,
    private val numFactory: NumFactory = defaultNumFactory,
) : AnalysisCriterion {

    companion object {
        fun buy(marketEvents: List<MarketEvent>): EnterAndHoldReturnCriterion =
            EnterAndHoldReturnCriterion(marketEvents, TradeType.BUY)

        fun sell(marketEvents: List<MarketEvent>): EnterAndHoldReturnCriterion =
            EnterAndHoldReturnCriterion(marketEvents, TradeType.SELL)
    }

    override fun calculate(position: Position): Num {
        if (marketEvents.isEmpty()) {
            return defaultNumFactory.one()
        }

        return calculate()
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        if (marketEvents.isEmpty()) {
            return defaultNumFactory.one()
        }

        return calculate()
    }

    private fun calculate(): Num {
        val firstPrice = (marketEvents.first { it is CandleReceived } as CandleReceived).closePrice
        val lastPrice = (marketEvents.last { it is CandleReceived } as CandleReceived).closePrice

        return numFactory.numOf(
            when (tradeType) {
                TradeType.BUY -> lastPrice / firstPrice
                TradeType.SELL -> {
                    2.0 - (lastPrice / firstPrice)
                }
            }
        )
    }
}
