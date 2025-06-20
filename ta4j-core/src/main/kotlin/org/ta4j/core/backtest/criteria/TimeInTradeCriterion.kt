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
        return tradingRecord.positions
            .stream()
            .filter(Position::isClosed)
            .map(this::calculate)
            .reduce(
                defaultNumFactory.zero(),
                { obj: Num, augend: Num -> obj.plus(augend) }
            ) // FIXME it adds overlapping periods
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
