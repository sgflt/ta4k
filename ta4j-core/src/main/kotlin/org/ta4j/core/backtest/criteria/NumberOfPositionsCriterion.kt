/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

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
        return defaultNumFactory.one()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        return defaultNumFactory.numOf(tradingRecord.positionCount)
    }
}
