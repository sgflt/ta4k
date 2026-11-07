/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.helpers

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num

/**
 * Standard deviation criterion.
 *
 *
 *
 * Calculates the standard deviation for a Criterion.
 */
class StandardDeviationCriterion : AnalysisCriterion {
    /**
     * If true, then the lower the criterion value the better, otherwise the higher
     * the criterion value the better. This property is only used for
     * [.betterThan].
     */
    private val lessIsBetter: Boolean

    private val varianceCriterion: VarianceCriterion


    /**
     * Constructor with [.lessIsBetter] = false.
     *
     * @param criterion the criterion from which the "standard deviation" is
     * calculated
     */
    constructor(criterion: AnalysisCriterion) {
        this.varianceCriterion = VarianceCriterion(criterion)
        this.lessIsBetter = false
    }


    /**
     * Constructor.
     *
     * @param criterion the criterion from which the "standard deviation" is
     * calculated
     * @param lessIsBetter the [.lessIsBetter]
     */
    constructor(criterion: AnalysisCriterion, lessIsBetter: Boolean) {
        this.varianceCriterion = VarianceCriterion(criterion)
        this.lessIsBetter = lessIsBetter
    }


    override fun calculate(position: Position): Num {
        return this.varianceCriterion.calculate(position).sqrt()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.positions.isEmpty()) {
            return tradingRecord.numFactory.zero()
        }
        return this.varianceCriterion.calculate(tradingRecord).sqrt()
    }
}
