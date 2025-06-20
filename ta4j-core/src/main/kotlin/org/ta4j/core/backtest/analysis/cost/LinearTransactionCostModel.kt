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

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.Trade
import org.ta4j.core.num.Num

/**
 * With this cost model, the trading costs for opening or closing a position
 * accrue linearly.
 */
class LinearTransactionCostModel
/**
 * Constructor with `feePerPosition * x`.
 *
 * @param feePerPosition the feePerPosition coefficient (e.g. 0.005 for 0.5% per
 * [trade][Trade])
 */(
    /** The slope of the linear model (fee per position).  */
    private val feePerPosition: Double,
) : CostModel {
    /**
     * @param position the position
     * @param finalIndex current bar index (irrelevant for the
     * LinearTransactionCostModel)
     *
     * @return the trading cost of the single `position`
     */
    override fun calculate(position: Position, finalIndex: Int): Num {
        return calculate(position)
    }


    override fun calculate(position: Position): Num {
        var totalPositionCost = position.numFactory.zero()
        val entryTrade = position.entry
        if (entryTrade != null) {
            // transaction costs of the entry trade
            totalPositionCost = entryTrade.cost
            if (position.exit != null) {
                totalPositionCost = totalPositionCost.plus(position.exit!!.cost)
            }
        }
        return totalPositionCost
    }


    override fun calculate(price: Num, amount: Num): Num {
        return amount.numFactory.numOf(this.feePerPosition) * price * amount
    }
}
