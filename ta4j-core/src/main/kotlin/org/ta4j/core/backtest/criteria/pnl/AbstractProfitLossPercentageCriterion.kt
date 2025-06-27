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
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Base class for profit and loss percentage criteria.
 *
 * For individual positions: Calculated as (profit / entry_value) * 100
 * For trading records: Calculated as (total_profit / total_invested) * 100
 *
 * @see <a href="https://www.investopedia.com/ask/answers/how-do-you-calculate-percentage-gain-or-loss-investment/">
 *      How to Calculate Percentage Gain or Loss on Investment</a>
 */
abstract class AbstractProfitLossPercentageCriterion : AnalysisCriterion {

    override fun calculate(position: Position): Num {
        if (!position.isClosed) {
            return defaultNumFactory.zero()
        }

        val entry = position.entry ?: return defaultNumFactory.zero()
        val entryValue = calculateEntryValue(position, entry)

        if (entryValue.isZero) {
            return defaultNumFactory.zero()
        }

        val profit = calculateProfit(position)
        return profit / entryValue * defaultNumFactory.hundred()
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        val closedPositions = tradingRecord.positions.filter { it.isClosed }

        if (closedPositions.isEmpty()) {
            return defaultNumFactory.zero()
        }

        // Calculate total invested capital and total profit
        var totalInvested = defaultNumFactory.zero()
        var totalProfit = defaultNumFactory.zero()

        for (position in closedPositions) {
            val entry = position.entry!!
            val entryValue = calculateEntryValue(position, entry)

            totalInvested += entryValue
            totalProfit += calculateProfit(position)
        }

        // Avoid division by zero
        if (totalInvested.isZero) {
            return defaultNumFactory.zero()
        }

        // Calculate overall percentage: (total_profit / total_invested) * 100
        return totalProfit / totalInvested * defaultNumFactory.hundred()
    }

    /**
     * Calculates the profit for the given position.
     * 
     * @param position the closed position
     * @return the profit (net or gross) for the position
     */
    protected abstract fun calculateProfit(position: Position): Num

    /**
     * Calculates the entry value for the given position.
     * 
     * @param position the position
     * @param entry the entry trade
     * @return the entry value (net or gross) for the position
     */
    protected abstract fun calculateEntryValue(position: Position, entry: org.ta4j.core.backtest.Trade): Num
}