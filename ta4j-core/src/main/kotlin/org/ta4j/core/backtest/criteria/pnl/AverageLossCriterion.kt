/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.backtest.criteria.NumberOfLosingPositionsCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Average gross loss criterion (includes trading costs).
 */
class AverageLossCriterion : AnalysisCriterion {
    private val grossLossCriterion = LossCriterion(false)
    private val numberOfLosingPositionsCriterion = NumberOfLosingPositionsCriterion()


    override fun calculate(position: Position): Num {
        val numberOfLosingPositions = this.numberOfLosingPositionsCriterion.calculate(position)
        if (numberOfLosingPositions.isZero) {
            return defaultNumFactory.zero()
        }
        val grossLoss = this.grossLossCriterion.calculate(position)
        if (grossLoss.isZero) {
            return defaultNumFactory.zero()
        }
        return grossLoss / numberOfLosingPositions
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val numberOfLosingPositions = this.numberOfLosingPositionsCriterion.calculate(tradingRecord)
        if (numberOfLosingPositions.isZero) {
            return defaultNumFactory.zero()
        }
        val grossLoss = this.grossLossCriterion.calculate(tradingRecord)
        if (grossLoss.isZero) {
            return defaultNumFactory.zero()
        }
        return grossLoss / numberOfLosingPositions
    }
}
