/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion.PositionFilter
import org.ta4j.core.num.Num

/**
 * Calculates the percentage of losing or winning positions, returned in decimal
 * format.
 *
 *
 *  * For [.positionFilter] = [PositionFilter.PROFIT]:
 * `number of winning positions / total number of positions`
 *  * For [.positionFilter] = [PositionFilter.LOSS]:
 * `number of losing positions / total number of positions`
 *
 */
class PositionsRatioCriterion(private val positionFilter: PositionFilter) : AnalysisCriterion {
    private var numberOfPositionsCriterion: AnalysisCriterion = if (positionFilter == PositionFilter.PROFIT) {
        NumberOfWinningPositionsCriterion()
    } else {
        NumberOfLosingPositionsCriterion()
    }

    override fun calculate(position: Position): Num = numberOfPositionsCriterion.calculate(position)


    override fun calculate(tradingRecord: TradingRecord): Num {
        val numberOfPositions = numberOfPositionsCriterion.calculate(tradingRecord)
        return numberOfPositions / tradingRecord.numFactory.numOf(tradingRecord.positionCount)
    }

    companion object {
        /**
         * @return [PositionsRatioCriterion] with [PositionFilter.PROFIT]
         */
        @JvmStatic
        fun winningPositionsRatioCriterion(): PositionsRatioCriterion {
            return PositionsRatioCriterion(PositionFilter.PROFIT)
        }


        /**
         * @return [PositionsRatioCriterion] with [PositionFilter.LOSS]
         */
        @JvmStatic
        fun losingPositionsRatioCriterion(): PositionsRatioCriterion {
            return PositionsRatioCriterion(PositionFilter.LOSS)
        }
    }
}
