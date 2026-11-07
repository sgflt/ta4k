/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.TradeType
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Versus "enter and hold" criterion, returned in decimal format.
 *
 * Compares the value of a provided [AnalysisCriterion] criterion with the
 * value of an "enter and hold". The "enter and hold"-strategy is done as
 * follows:
 *
 *
 * - For [tradeType] = [TradeType.BUY]: Buy with the close price
 * of the first bar and sell with the close price of the last bar.
 * - For [tradeType] = [TradeType.SELL]: Sell with the close
 * price of the first bar and buy with the close price of the last bar.
 *
 */
class VersusEnterAndHoldCriterion(
    private val marketEvents: List<MarketEvent>,
    private val tradeType: TradeType,
    private val criterion: AnalysisCriterion,
) :
    AnalysisCriterion {

    override fun calculate(position: Position): Num {
        val fakeRecord = createEnterAndHoldTradingRecord(position.numFactory)
        return criterion.calculate(position) / criterion.calculate(fakeRecord)
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        val positions = tradingRecord.positions
        val fakeRecord = createEnterAndHoldTradingRecord(tradingRecord.numFactory)

        if (positions.isEmpty()) {
            // No positions case - compare 1 (no change) vs buy-and-hold from start to end
            return tradingRecord.numFactory.one() / criterion.calculate(fakeRecord)
        }

        return criterion.calculate(tradingRecord) / criterion.calculate(fakeRecord)
    }

    private fun createEnterAndHoldTradingRecord(numFactory: NumFactory): TradingRecord {
        val fakeRecord = BackTestTradingRecord(
            startingType = tradeType,
            numFactory = numFactory,
            name = "enter-and-hold",
        )

        val entry = marketEvents.first { it is CandleReceived } as CandleReceived
        val exit = marketEvents.last { it is CandleReceived } as CandleReceived
        fakeRecord.enter(entry.beginTime, numFactory.numOf(entry.closePrice), numFactory.one())
        fakeRecord.exit(exit.beginTime, numFactory.numOf(exit.closePrice), numFactory.one())

        return fakeRecord
    }
}
