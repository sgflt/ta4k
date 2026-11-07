/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.execution

import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num

/**
 * Sizes new entries. Exits always close the whole open amount and do not ask the sizer.
 */
fun interface PositionSizer {
    /**
     * @param price the entry fill price, slippage included
     * @param tradingRecord the record the entry goes into
     * @return number of units to buy (or sell short)
     */
    fun size(price: Num, tradingRecord: TradingRecord): Num

    companion object {
        /** Always [units] units. */
        fun fixedUnits(units: Number) = PositionSizer { price, _ -> price.numFactory.numOf(units) }

        /** As many units as [cash] buys at the fill price. */
        fun fixedCash(cash: Number) = PositionSizer { price, _ -> price.numFactory.numOf(cash) / price }

        /**
         * [fraction] of current equity, where equity is [initialCapital] grown by the record's realized
         * returns (its equity curve, or closed positions' profits if it keeps no curve).
         */
        fun fractionOfEquity(initialCapital: Number, fraction: Number): PositionSizer {
            require(fraction.toDouble() > 0.0 && fraction.toDouble() <= 1.0) { "Fraction must be in (0, 1]" }
            return PositionSizer { price, record ->
                val numFactory = price.numFactory
                val capital = numFactory.numOf(initialCapital)
                val equity = record.equityCurve.lastOrNull()?.let { capital * it.equity }
                    ?: record.positions.fold(capital) { sum, position -> sum + position.profit }
                equity * numFactory.numOf(fraction) / price
            }
        }
    }
}
