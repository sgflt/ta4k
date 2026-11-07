/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num

/**
 * Ratio gross profit and loss criterion = Average gross profit (includes
 * trading costs) / Average gross loss (includes trading costs), returned in
 * decimal format.
 */
class ProfitLossRatioCriterion : AnalysisCriterion {
    private val averageProfitCriterion = AverageProfitCriterion()
    private val averageLossCriterion = AverageLossCriterion()


    override fun calculate(position: Position): Num {
        val averageProfit = this.averageProfitCriterion.calculate(position!!)
        if (averageProfit.isZero) {
            // only loosing positions means a ratio of 0
            return position.numFactory.zero()
        }
        val averageLoss = this.averageLossCriterion.calculate(position)
        if (averageLoss.isZero) {
            // only winning positions means a ratio of 1
            return position.numFactory.one()
        }
        return averageProfit / averageLoss
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val averageProfit = this.averageProfitCriterion.calculate(tradingRecord)
        if (averageProfit.isZero) {
            // only loosing positions means a ratio of 0
            return tradingRecord.numFactory.zero()
        }
        val averageLoss = this.averageLossCriterion.calculate(tradingRecord)
        if (averageLoss.isZero) {
            // only winning positions means a ratio of 1
            return tradingRecord.numFactory.one()
        }
        return averageProfit / averageLoss.abs()
    }
}
