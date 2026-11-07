/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Reward risk ratio criterion (also known as "RoMaD"), returned in decimal
 * format.
 *
 * <pre>
 * RoMaD = net return (without base) / maximum drawdown
 * </pre>
 */
class ReturnOverMaxDrawdownCriterion(private val numFactory: NumFactory) : AnalysisCriterion {
    private val netReturnCriterion = ReturnCriterion(false)
    private val maxDrawdownCriterion = MaximumDrawdownCriterion(numFactory)


    override fun calculate(position: Position): Num {
        if (position.isOpened) {
            return numFactory.zero()
        }

        val maxDrawdown = maxDrawdownCriterion.calculate(position)
        val netReturn = netReturnCriterion.calculate(position)
        return if (maxDrawdown.isZero) {
            netReturn
        } else {
            netReturn / maxDrawdown
        }
    }

    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.positions.isEmpty()) {
            return numFactory.zero() // penalise no-trade strategies
        }

        val maxDrawdown = maxDrawdownCriterion.calculate(tradingRecord)
        val netReturn = netReturnCriterion.calculate(tradingRecord)
        return if (maxDrawdown.isZero) {
            netReturn // perfect equity curve
        } else {
            netReturn / maxDrawdown // regular RoMaD
        }
    }
}
