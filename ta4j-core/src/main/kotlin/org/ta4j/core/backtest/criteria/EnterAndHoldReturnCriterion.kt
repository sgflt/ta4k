/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.TradeType
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

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
) : AnalysisCriterion {

    companion object {
        fun buy(marketEvents: List<MarketEvent>): EnterAndHoldReturnCriterion =
            EnterAndHoldReturnCriterion(marketEvents, TradeType.BUY)

        fun sell(marketEvents: List<MarketEvent>): EnterAndHoldReturnCriterion =
            EnterAndHoldReturnCriterion(marketEvents, TradeType.SELL)
    }

    override fun calculate(position: Position): Num {
        if (marketEvents.isEmpty()) {
            return position.numFactory.one()
        }

        return calculate(position.numFactory)
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        if (marketEvents.isEmpty()) {
            return tradingRecord.numFactory.one()
        }

        return calculate(tradingRecord.numFactory)
    }

    private fun calculate(numFactory: NumFactory): Num {
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
