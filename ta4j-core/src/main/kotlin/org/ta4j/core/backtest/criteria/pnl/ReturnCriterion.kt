/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Return (in percentage) criterion (includes trading costs), returned in
 * decimal format.
 *
 * The return of the provided [position(s)][Position]
 */
class ReturnCriterion : AnalysisCriterion {
    /**
     * If true, then the base percentage of `1` (equivalent to 100%) is added
     * to the criterion value.
     */
    private val addBase: Boolean


    /**
     * Constructor with [.addBase] == true.
     */
    constructor() {
        addBase = true
    }


    /**
     * Constructor.
     *
     * @param addBase the [.addBase]
     */
    constructor(addBase: Boolean) {
        this.addBase = addBase
    }


    override fun calculate(position: Position): Num {
        return calculateProfit(position)
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val product = tradingRecord.positions
            .map { calculateProfit(it) }
            .fold(defaultNumFactory.one()) { acc, multiplicand -> acc * multiplicand }

        return product - (if (addBase) defaultNumFactory.zero() else defaultNumFactory.one())
    }


    /**
     * Calculates the gross return of a position (Buy and sell).
     *
     * @param position a position
     *
     * @return the gross return of the position
     */
    private fun calculateProfit(position: Position): Num {
        if (position.isClosed) {
            return position.grossReturn
        }
        return if (this.addBase)
            defaultNumFactory.one()
        else
            defaultNumFactory.zero()
    }
}
