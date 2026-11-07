/*
 * SPDX-License-Identifier: MIT
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


    private fun calculate(
        profitLossRatio: Num, numberOfWinningPositions: Num,
        numberOfAllPositions: Num,
    ): Num {
        if (numberOfAllPositions.isZero || profitLossRatio.isZero) {
            return defaultNumFactory.zero()
        }
        // Expectancy = ((1 + AW/AL) * ProbabilityToWinOnePosition) - 1
        val one = defaultNumFactory.one()
        val probabiltyToWinOnePosition = numberOfWinningPositions / numberOfAllPositions
        return (one + profitLossRatio) * probabiltyToWinOnePosition - one
    }
}
