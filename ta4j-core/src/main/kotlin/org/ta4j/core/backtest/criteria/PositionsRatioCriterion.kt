/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
import org.ta4j.core.backtest.criteria.AnalysisCriterion.PositionFilter
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

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
        return numberOfPositions / defaultNumFactory.numOf(tradingRecord.positionCount)
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
