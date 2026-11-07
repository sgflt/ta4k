/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.backtest.criteria.NumberOfWinningPositionsCriterion
import org.ta4j.core.num.Num

/**
 * Average gross profit criterion (includes trading costs).
 */
class AverageProfitCriterion : AnalysisCriterion {
    private val grossProfitCriterion = ProfitCriterion(false)
    private val numberOfWinningPositionsCriterion = NumberOfWinningPositionsCriterion()


    override fun calculate(position: Position): Num {
        val numberOfWinningPositions = numberOfWinningPositionsCriterion.calculate(position)
        if (numberOfWinningPositions.isZero) {
            return position.numFactory.zero()
        }
        val grossProfit = grossProfitCriterion.calculate(position)
        if (grossProfit.isZero) {
            return position.numFactory.zero()
        }
        return grossProfit / numberOfWinningPositions
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val numberOfWinningPositions = numberOfWinningPositionsCriterion.calculate(tradingRecord)
        if (numberOfWinningPositions.isZero) {
            return tradingRecord.numFactory.zero()
        }
        val grossProfit = grossProfitCriterion.calculate(tradingRecord)
        if (grossProfit.isZero) {
            return tradingRecord.numFactory.zero()
        }
        return grossProfit / numberOfWinningPositions
    }
}
