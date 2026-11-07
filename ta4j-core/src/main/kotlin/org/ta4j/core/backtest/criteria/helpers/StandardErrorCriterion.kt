/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.helpers

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.backtest.criteria.NumberOfPositionsCriterion
import org.ta4j.core.num.Num

/**
 * Standard error criterion.
 *
 *
 *
 * Calculates the standard error for a Criterion.
 */
class StandardErrorCriterion : AnalysisCriterion {
    /**
     * If true, then the lower the criterion value the better, otherwise the higher
     * the criterion value the better. This property is only used for
     * [.betterThan].
     */
    private val lessIsBetter: Boolean

    private val standardDeviationCriterion: StandardDeviationCriterion
    private val numberOfPositionsCriterion = NumberOfPositionsCriterion()


    /**
     * Constructor with [.lessIsBetter] = true.
     *
     * @param criterion the criterion from which the "standard deviation error" is
     * calculated
     */
    constructor(criterion: AnalysisCriterion) {
        standardDeviationCriterion = StandardDeviationCriterion(criterion)
        lessIsBetter = true
    }


    /**
     * Constructor.
     *
     * @param criterion the criterion from which the "standard deviation error"
     * is calculated
     * @param lessIsBetter the [.lessIsBetter]
     */
    constructor(criterion: AnalysisCriterion, lessIsBetter: Boolean) {
        standardDeviationCriterion = StandardDeviationCriterion(criterion)
        this.lessIsBetter = lessIsBetter
    }


    override fun calculate(position: Position): Num {
        val numberOfPositions = numberOfPositionsCriterion.calculate(position)
        return standardDeviationCriterion.calculate(position) / numberOfPositions.sqrt()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.positions.isEmpty()) {
            return tradingRecord.numFactory.zero()
        }
        val numberOfPositions = numberOfPositionsCriterion.calculate(tradingRecord)
        return standardDeviationCriterion.calculate(tradingRecord) / numberOfPositions.sqrt()
    }
}
