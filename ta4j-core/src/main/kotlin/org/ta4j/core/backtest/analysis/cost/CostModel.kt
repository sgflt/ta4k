/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.analysis.cost

import org.ta4j.core.backtest.Position
import org.ta4j.core.num.Num

/**
 * With the `CostModel`, we can include trading costs that may be incurred
 * when opening or closing a position.
 */
interface CostModel {
    /**
     * @param position   the position
     * @param finalIndex the index up to which open positions are considered
     * @return the trading cost of the single `position`
     */
    fun calculate(position: Position, finalIndex: Int): Num

    /**
     * @param position the position
     * @return the trading cost of the single `position`
     */
    fun calculate(position: Position): Num

    /**
     * @param price  the trade price per asset
     * @param amount the trade amount (i.e. the number of traded assets)
     * @return the trading cost for the traded `amount`
     */
    fun calculate(price: Num, amount: Num): Num
}
