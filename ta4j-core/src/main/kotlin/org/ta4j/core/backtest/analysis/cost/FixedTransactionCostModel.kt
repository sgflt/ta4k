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
