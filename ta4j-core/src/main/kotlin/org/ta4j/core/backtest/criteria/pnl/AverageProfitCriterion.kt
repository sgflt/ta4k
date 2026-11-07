/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.backtest.criteria.NumberOfWinningPositionsCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Average gross profit criterion (includes trading costs).
 */
class AverageProfitCriterion : AnalysisCriterion {
    private val grossProfitCriterion = ProfitCriterion(false)
    private val numberOfWinningPositionsCriterion = NumberOfWinningPositionsCriterion()


    override fun calculate(position: Position): Num {
        val numberOfWinningPositions = numberOfWinningPositionsCriterion.calculate(position)
        if (numberOfWinningPositions.isZero) {
            return defaultNumFactory.zero()
        }
        val grossProfit = grossProfitCriterion.calculate(position)
        if (grossProfit.isZero) {
            return defaultNumFactory.zero()
        }
        return grossProfit / numberOfWinningPositions
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val numberOfWinningPositions = numberOfWinningPositionsCriterion.calculate(tradingRecord)
        if (numberOfWinningPositions.isZero) {
            return defaultNumFactory.zero()
        }
        val grossProfit = grossProfitCriterion.calculate(tradingRecord)
        if (grossProfit.isZero) {
            return defaultNumFactory.zero()
        }
        return grossProfit / numberOfWinningPositions
    }
}
