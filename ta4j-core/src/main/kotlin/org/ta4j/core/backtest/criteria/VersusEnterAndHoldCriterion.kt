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
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider

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
        val fakeRecord = createEnterAndHoldTradingRecord()
        return criterion.calculate(position) / criterion.calculate(fakeRecord)
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        val positions = tradingRecord.positions
        val fakeRecord = createEnterAndHoldTradingRecord(
        )

        if (positions.isEmpty()) {
            // No positions case - compare 1 (no change) vs buy-and-hold from start to end
            return NumFactoryProvider.defaultNumFactory.one() / criterion.calculate(fakeRecord)
        }

        return criterion.calculate(tradingRecord) / criterion.calculate(fakeRecord)
    }

    private fun createEnterAndHoldTradingRecord(): TradingRecord {
        val numFactory = NumFactoryProvider.defaultNumFactory
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
