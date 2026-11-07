/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.helpers

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.backtest.criteria.NumberOfPositionsCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Average criterion.
 *
 *
 *
 * Calculates the average of a Criterion by dividing it by the number of
 * positions.
 */
class AverageCriterion : AnalysisCriterion {
    /**
     * If true, then the lower the criterion value the better, otherwise the higher
     * the criterion value the better. This property is only used for
     * [.betterThan].
     */
    private val lessIsBetter: Boolean

    private val criterion: AnalysisCriterion
    private val numberOfPositionsCriterion = NumberOfPositionsCriterion()


    /**
     * Constructor with [.lessIsBetter] = false.
     *
     * @param criterion the criterion from which the "average" is calculated
     */
    constructor(criterion: AnalysisCriterion) {
        this.criterion = criterion
        lessIsBetter = false
    }


    /**
     * Constructor.
     *
     * @param criterion the criterion from which the "average" is calculated
     * @param lessIsBetter the [.lessIsBetter]
     */
    constructor(criterion: AnalysisCriterion, lessIsBetter: Boolean) {
        this.criterion = criterion
        this.lessIsBetter = lessIsBetter
    }


    override fun calculate(position: Position): Num {
        val numberOfPositions = numberOfPositionsCriterion.calculate(position)
        return criterion.calculate(position) / numberOfPositions
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.positions.isEmpty()) {
            return defaultNumFactory.zero()
        }
        val numberOfPositions = numberOfPositionsCriterion.calculate(tradingRecord)
        return criterion.calculate(tradingRecord) / numberOfPositions
    }
}
