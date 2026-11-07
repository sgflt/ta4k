/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.helpers.StandardDeviationCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.num.Num

/**
 * The SQN ("System Quality Number") Criterion.
 *
 * @see [https://indextrader.com.au/van-tharps-sqn/](https://indextrader.com.au/van-tharps-sqn/)
 */
class SqnCriterion @JvmOverloads constructor(
    private val criterion: AnalysisCriterion = ProfitLossCriterion(),
    /**
     * The number to be used for the part of `√(numberOfPositions)` within the
     * SQN-Formula when there are more than 100 trades. If this value is
     * `null`, then the number of positions calculated by
     * [.numberOfPositionsCriterion] is used instead.
     */
    private val nPositions: Int? = null,
) : AnalysisCriterion {
    private val standardDeviationCriterion = StandardDeviationCriterion(criterion)
    private val numberOfPositionsCriterion = NumberOfPositionsCriterion()


    override fun calculate(position: Position): Num {
        val stdDevPnl = standardDeviationCriterion.calculate(position)
        if (stdDevPnl.isZero) {
            return position.numFactory.zero()
        }
        // SQN = (Average (PnL) / StdDev(PnL)) * SquareRoot(NumberOfTrades)
        val numberOfPositions = numberOfPositionsCriterion.calculate(position)
        val pnl = criterion.calculate(position)
        val avgPnl = pnl / numberOfPositions
        return avgPnl / stdDevPnl * numberOfPositions.sqrt()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.positions.isEmpty()) {
            return tradingRecord.numFactory.zero()
        }
        val stdDevPnl = standardDeviationCriterion.calculate(tradingRecord)
        if (stdDevPnl.isZero) {
            return tradingRecord.numFactory.zero()
        }

        var numberOfPositions = numberOfPositionsCriterion.calculate(tradingRecord)
        val pnl = criterion.calculate(tradingRecord)
        val avgPnl = pnl / numberOfPositions
        if (nPositions != null && numberOfPositions > tradingRecord.numFactory.hundred()
        ) {
            numberOfPositions = tradingRecord.numFactory.numOf(nPositions)
        }
        // SQN = (Average (PnL) / StdDev(PnL)) * SquareRoot(NumberOfTrades)
        return avgPnl / stdDevPnl * numberOfPositions.sqrt()
    }
}
