/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.Trade
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * A linear transaction cost criterion.
 *
 *
 *
 * Calculates the transaction cost according to an initial traded amount and a
 * linear function defined by a and b (a * x + b).
 */
class LinearTransactionCostCriterion @JvmOverloads constructor(
    private val initialAmount: Double,
    private val a: Double,
    private val b: Double = 0.0,
) : AnalysisCriterion {
    private val grossReturn: ReturnCriterion


    /**
     * Constructor. (a * x + b)
     *
     * @param initialAmount the initially traded amount
     * @param a the a coefficient (e.g. 0.005 for 0.5% per [     trade][Trade])
     * @param b the b constant (e.g. 0.2 for $0.2 per [     trade][Trade])
     */
    /**
     * Constructor. (a * x)
     *
     * @param initialAmount the initially traded amount
     * @param a the a coefficient (e.g. 0.005 for 0.5% per [     trade][Trade])
     */
    init {
        this.grossReturn = ReturnCriterion()
    }


    override fun calculate(position: Position): Num {
        return getTradeCost(position, defaultNumFactory.numOf(this.initialAmount))
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        var totalCosts = defaultNumFactory.zero()
        var tradedAmount = defaultNumFactory.numOf(this.initialAmount)

        for (position in tradingRecord.positions) {
            val tradeCost = getTradeCost(position, tradedAmount)
            totalCosts = totalCosts.plus(tradeCost)
            // To calculate the new traded amount:
            // - Remove the cost of the *first* trade
            // - Multiply by the profit ratio
            // - Remove the cost of the *second* trade
            tradedAmount = tradedAmount - getTradeCost(position.entry, tradedAmount)
            tradedAmount = tradedAmount * this.grossReturn.calculate(position)
            tradedAmount = tradedAmount - getTradeCost(position.exit, tradedAmount)
        }

        // Special case: if the current position is open
        val currentPosition = tradingRecord.currentPosition
        if (currentPosition.isOpened) {
            totalCosts = totalCosts.plus(getTradeCost(currentPosition.entry, tradedAmount))
        }

        return totalCosts
    }

    /**
     * @param trade the trade
     * @param tradedAmount the amount of the trade
     *
     * @return the absolute trade cost
     */
    private fun getTradeCost(trade: Trade?, tradedAmount: Num): Num {
        val numFactory = tradedAmount.numFactory
        val tradeCost = numFactory.zero()
        if (trade != null) {
            return numFactory.numOf(this.a) * tradedAmount + numFactory.numOf(this.b)
        }
        return tradeCost
    }


    /**
     * @param position the position
     * @param initialAmount the initially traded amount for the position
     *
     * @return the absolute total cost of all trades in the position
     */
    private fun getTradeCost(position: Position?, initialAmount: Num): Num {
        var totalTradeCost = defaultNumFactory.zero()
        if (position != null && position.entry != null) {
            totalTradeCost = getTradeCost(position.entry, initialAmount)
            if (position.exit != null) {
                // To calculate the new traded amount:
                // - Remove the cost of the first trade
                // - Multiply by the profit ratio
                val newTradedAmount = (initialAmount - totalTradeCost) * this.grossReturn.calculate(position)
                totalTradeCost += getTradeCost(position.exit, newTradedAmount)
            }
        }
        return totalTradeCost
    }
}
