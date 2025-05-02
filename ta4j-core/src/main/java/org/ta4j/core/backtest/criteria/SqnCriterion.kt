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
import org.ta4j.core.backtest.criteria.helpers.StandardDeviationCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

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
            return defaultNumFactory.zero()
        }
        // SQN = (Average (PnL) / StdDev(PnL)) * SquareRoot(NumberOfTrades)
        val numberOfPositions = numberOfPositionsCriterion.calculate(position)
        val pnl = criterion.calculate(position)
        val avgPnl = pnl.dividedBy(numberOfPositions)
        return avgPnl.dividedBy(stdDevPnl).multipliedBy(numberOfPositions.sqrt())
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.positions.isEmpty()) {
            return defaultNumFactory.zero()
        }
        val stdDevPnl = standardDeviationCriterion.calculate(tradingRecord)
        if (stdDevPnl.isZero) {
            return defaultNumFactory.zero()
        }

        var numberOfPositions = numberOfPositionsCriterion.calculate(tradingRecord)
        val pnl = criterion.calculate(tradingRecord)
        val avgPnl = pnl.dividedBy(numberOfPositions)
        if (nPositions != null && numberOfPositions.isGreaterThan(
                defaultNumFactory
                    .hundred()
            )
        ) {
            numberOfPositions = defaultNumFactory.numOf(nPositions)
        }
        // SQN = (Average (PnL) / StdDev(PnL)) * SquareRoot(NumberOfTrades)
        return avgPnl.dividedBy(stdDevPnl).multipliedBy(numberOfPositions.sqrt())
    }


    /** The higher the criterion value, the better.  */
    override fun betterThan(criterionValue1: Num, criterionValue2: Num): Boolean {
        return criterionValue1.isGreaterThan(criterionValue2)
    }
}
