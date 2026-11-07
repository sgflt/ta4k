/*
 * SPDX-License-Identifier: MIT
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
