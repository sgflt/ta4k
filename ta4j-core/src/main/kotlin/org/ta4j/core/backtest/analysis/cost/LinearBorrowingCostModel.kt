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
package org.ta4j.core.backtest.analysis.cost

import java.time.temporal.ChronoUnit
import org.ta4j.core.backtest.Position
import org.ta4j.core.num.Num

/**
 * With this cost model, the trading costs for borrowing a position (i.e.
 * selling a position short) accrue linearly.
 */
class LinearBorrowingCostModel
/**
 * Constructor with `feePerPeriod * nPeriod`.
 *
 * @param feePerPeriod the coefficient (e.g. 0.0001 for 1bp per period)
 */(
    /** The slope of the linear model (fee per period).  */
    private val feePerPeriod: Double,
) : CostModel {
    /**
     * @return always `0`, as borrowing costs depend on borrowed period
     */
    override fun calculate(price: Num, amount: Num): Num {
        return price.numFactory.zero()
    }


    override fun calculate(position: Position, finalIndex: Int): Num {
        return calculate(position)
    }


    override fun calculate(position: Position): Num {
        val tradingPeriods = calculateTradingPeriods(position)
        return calculateBorrowingCost(tradingPeriods, position.entry!!.value)
    }


    private fun calculateTradingPeriods(position: Position): Long {
        return position.getTimeInTrade(ChronoUnit.DAYS)
    }


    private fun calculateBorrowingCost(tradingPeriods: Long, tradedValue: Num): Num {
        val numFactory = tradedValue.numFactory
        return tradedValue * numFactory.numOf(tradingPeriods) * numFactory.numOf(this.feePerPeriod)
    }
}
