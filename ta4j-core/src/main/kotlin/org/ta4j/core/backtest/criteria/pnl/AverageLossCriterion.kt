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
