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
 * Variance criterion.
 *
 *
 *
 * Calculates the variance for a Criterion.
 */
class VarianceCriterion : AnalysisCriterion {
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
     * @param criterion the criterion from which the "variance" is calculated
     */
    constructor(criterion: AnalysisCriterion) {
        this.criterion = criterion
        this.lessIsBetter = false
    }


    /**
     * Constructor.
     *
     * @param criterion the criterion from which the "variance" is calculated
     * @param lessIsBetter the [.lessIsBetter]
     */
    constructor(criterion: AnalysisCriterion, lessIsBetter: Boolean) {
        this.criterion = criterion
        this.lessIsBetter = lessIsBetter
    }


    override fun calculate(position: Position): Num {
        val criterionValue = this.criterion.calculate(position)
        val numberOfPositions = this.numberOfPositionsCriterion.calculate(position)

        var variance = position.numFactory.zero()
        val average = criterionValue / numberOfPositions
        val pow = this.criterion.calculate(position).minus(average).pow(2)
        variance = variance + pow
        variance = variance / numberOfPositions
        return variance
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.positions.isEmpty()) {
            return tradingRecord.numFactory.zero()
        }
        val criterionValue = this.criterion.calculate(tradingRecord)
        val numberOfPositions = this.numberOfPositionsCriterion.calculate(tradingRecord)

        var variance = tradingRecord.numFactory.zero()
        val average = criterionValue / numberOfPositions

        for (position in tradingRecord.positions) {
            val pow = this.criterion.calculate(position).minus(average).pow(2)
            variance = variance + pow
        }
        variance = variance / numberOfPositions
        return variance
    }
}
