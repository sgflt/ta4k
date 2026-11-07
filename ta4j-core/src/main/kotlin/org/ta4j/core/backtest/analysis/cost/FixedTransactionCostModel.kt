/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.analysis.cost

import org.ta4j.core.backtest.Position
import org.ta4j.core.num.Num

/**
 * With this cost model, the trading costs for opening or closing a position are
 * accrued through a constant fee per trade (i.e. a fixed fee per transaction).
 */
open class FixedTransactionCostModel
/**
 * Constructor for a fixed fee trading cost model.
 *
 * <pre>
 * Cost of opened [position][Position]: (fixedFeePerTrade * 1)
 * Cost of closed [position][Position]: (fixedFeePerTrade * 2)
</pre> *
 *
 * @param feePerTrade the fixed fee per [trade][Trade]
 */(
    /** The fixed fee per [trade][Trade].  */
    private val feePerTrade: Double,
) : CostModel {
    /**
     * @param position     the position
     * @param finalIndex the current bar index (irrelevant for
     * `FixedTransactionCostModel`)
     * @return the transaction cost of the single `position`
     */
    override fun calculate(position: Position, finalIndex: Int): Num {
        val numFactory = position.entry!!.pricePerAsset.numFactory
        var multiplier = numFactory.one()
        if (position.isClosed) {
            multiplier = numFactory.numOf(2)
        }
        return numFactory.numOf(this.feePerTrade) * multiplier
    }

    /**
     * @return the transaction cost of the single `position`
     */
    override fun calculate(position: Position): Num {
        return this.calculate(position, 0)
    }

    /**
     * **Note:** Both `price` and `amount` are irrelevant as the fee
     * in `FixedTransactionCostModel` is always the same.
     *
     * @return [.feePerTrade]
     */
    override fun calculate(price: Num, amount: Num): Num {
        return price.numFactory.numOf(this.feePerTrade)
    }
}
