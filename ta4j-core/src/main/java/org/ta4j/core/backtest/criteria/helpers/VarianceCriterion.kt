/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.backtest.criteria.helpers

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.backtest.criteria.NumberOfPositionsCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

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

        var variance = defaultNumFactory.zero()
        val average = criterionValue.dividedBy(numberOfPositions)
        val pow = this.criterion.calculate(position).minus(average).pow(2)
        variance = variance.plus(pow)
        variance = variance.dividedBy(numberOfPositions)
        return variance
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.positions.isEmpty()) {
            return defaultNumFactory.zero()
        }
        val criterionValue = this.criterion.calculate(tradingRecord)
        val numberOfPositions = this.numberOfPositionsCriterion.calculate(tradingRecord)

        var variance = defaultNumFactory.zero()
        val average = criterionValue.dividedBy(numberOfPositions)

        for (position in tradingRecord.positions) {
            val pow = this.criterion.calculate(position).minus(average).pow(2)
            variance = variance.plus(pow)
        }
        variance = variance.dividedBy(numberOfPositions)
        return variance
    }


    /**
     * If [.lessIsBetter] == false, then the lower the criterion value, the
     * better, otherwise the higher the criterion value the better.
     */
    override fun betterThan(criterionValue1: Num, criterionValue2: Num): Boolean {
        return if (this.lessIsBetter)
            criterionValue1.isLessThan(criterionValue2)
        else
            criterionValue1.isGreaterThan(criterionValue2)
    }
}
