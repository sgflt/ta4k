/*
 * SPDX-License-Identifier: MIT
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
