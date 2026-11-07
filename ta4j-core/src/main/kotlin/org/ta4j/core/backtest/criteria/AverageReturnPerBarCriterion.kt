/*
 * SPDX-License-Identifier: MIT
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
