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
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.pnl.ProfitLossRatioCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Expectancy criterion (also called "Kelly Criterion").
 *
 *
 *
 * Measures the positive or negative expectancy. The higher the positive number,
 * the better a winning expectation. A negative number means there is only
 * losing expectations.
 *
 * @see [https://www.straightforex.com/advanced-forex-course/money-management/two-important-things-to-be-considered/](https://www.straightforex.com/advanced-forex-course/money-management/two-important-things-to-be-considered/)
 */
class ExpectancyCriterion : AnalysisCriterion {
    private val profitLossRatioCriterion = ProfitLossRatioCriterion()
    private val numberOfPositionsCriterion = NumberOfPositionsCriterion()
    private val numberOfWinningPositionsCriterion = NumberOfWinningPositionsCriterion()


    override fun calculate(position: Position): Num {
        val profitLossRatio = this.profitLossRatioCriterion.calculate(position)
        val numberOfPositions = this.numberOfPositionsCriterion.calculate(position)
        val numberOfWinningPositions = this.numberOfWinningPositionsCriterion.calculate(position)
        return calculate(profitLossRatio, numberOfWinningPositions, numberOfPositions)
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val profitLossRatio = this.profitLossRatioCriterion.calculate(tradingRecord)
        val numberOfPositions = this.numberOfPositionsCriterion.calculate(tradingRecord)
        val numberOfWinningPositions = this.numberOfWinningPositionsCriterion.calculate(tradingRecord)
        return calculate(profitLossRatio, numberOfWinningPositions, numberOfPositions)
    }


    /** The higher the criterion value, the better.  */
    override fun betterThan(criterionValue1: Num, criterionValue2: Num): Boolean {
        return criterionValue1.isGreaterThan(criterionValue2)
    }


    private fun calculate(
        profitLossRatio: Num, numberOfWinningPositions: Num,
        numberOfAllPositions: Num,
    ): Num {
        if (numberOfAllPositions.isZero || profitLossRatio.isZero) {
            return defaultNumFactory.zero()
        }
        // Expectancy = ((1 + AW/AL) * ProbabilityToWinOnePosition) - 1
        val one = defaultNumFactory.one()
        val probabiltyToWinOnePosition = numberOfWinningPositions.dividedBy(numberOfAllPositions)
        return (one.plus(profitLossRatio)).multipliedBy(probabiltyToWinOnePosition).minus(one)
    }
}
