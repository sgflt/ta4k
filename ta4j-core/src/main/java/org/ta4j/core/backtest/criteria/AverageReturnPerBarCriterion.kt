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

import java.time.temporal.ChronoUnit
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Calculates the average return per bar criterion, returned in decimal format.
 *
 *
 *
 * It uses the following formula to accurately capture the compounding effect of
 * returns over the specified number of bars:
 *
 * <pre>
 * AverageReturnPerBar = pow([gross return][ReturnCriterion], 1/ [number of time units][TimeInTradeCriterion])
</pre> *
 */
class AverageReturnPerBarCriterion(private val numFactory: NumFactory, averagingUnit: ChronoUnit) : AnalysisCriterion {
    private val grossReturn = ReturnCriterion()
    private val timeInTradeCriterion = TimeInTradeCriterion(averagingUnit)


    override fun calculate(position: Position): Num {
        val bars = this.timeInTradeCriterion.calculate(position)
        // If a simple division was used (grossreturn/bars), compounding would not be
        // considered, leading to inaccuracies in the calculation.
        // Therefore we need to use "pow" to accurately capture the compounding effect.
        return if (bars.isZero)
            defaultNumFactory.one()
        else
            this.grossReturn.calculate(position).pow(defaultNumFactory.one() / bars)
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val timeUnits = this.timeInTradeCriterion.calculate(tradingRecord)
        return if (timeUnits.isZero)
            this.numFactory.one()
        else
            this.grossReturn.calculate(tradingRecord).pow(this.numFactory.one() / timeUnits)
    }
}
