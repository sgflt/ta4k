/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num

/**
 * Number of position criterion.
 */
class NumberOfPositionsCriterion : AnalysisCriterion {
    /**
     * If true, then the lower the criterion value the better, otherwise the higher
     * the criterion value the better. This property is only used for
     * [.betterThan].
     */
    private val lessIsBetter: Boolean


    /**
     * Constructor with [.lessIsBetter] = true.
     */
    constructor() {
        this.lessIsBetter = true
    }


    /**
     * Constructor.
     *
     * @param lessIsBetter the [.lessIsBetter]
     */
    constructor(lessIsBetter: Boolean) {
        this.lessIsBetter = lessIsBetter
    }


    override fun calculate(position: Position): Num {
        return position.numFactory.one()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        return tradingRecord.numFactory.numOf(tradingRecord.positionCount)
    }
}
