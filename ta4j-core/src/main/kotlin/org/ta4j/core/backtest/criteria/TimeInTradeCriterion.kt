/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import java.time.temporal.ChronoUnit
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Time in market criterion.
 *
 *
 *
 * Returns the total time in the market in seconds.
 */
class TimeInTradeCriterion(private val unit: ChronoUnit) : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        if (position.isClosed) {
            return defaultNumFactory.numOf(position.getTimeInTrade(this.unit))
        }

        return defaultNumFactory.zero()
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        // FIXME it adds overlapping periods
        return tradingRecord.positions
            .filter { it.isClosed }
            .fold(defaultNumFactory.zero()) { total, position -> total + calculate(position) }
    }

    companion object {
        fun seconds(): TimeInTradeCriterion {
            return TimeInTradeCriterion(ChronoUnit.SECONDS)
        }


        @JvmStatic
        fun minutes(): TimeInTradeCriterion {
            return TimeInTradeCriterion(ChronoUnit.MINUTES)
        }


        fun hours(): TimeInTradeCriterion {
            return TimeInTradeCriterion(ChronoUnit.HOURS)
        }


        fun days(): TimeInTradeCriterion {
            return TimeInTradeCriterion(ChronoUnit.DAYS)
        }
    }
}
