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
