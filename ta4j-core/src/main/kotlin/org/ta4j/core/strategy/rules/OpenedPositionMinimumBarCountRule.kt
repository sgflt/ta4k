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
package org.ta4j.core.strategy.rules

import java.time.Duration
import java.time.Instant
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.backtest.strategy.runtime.CurrentTimeResolver
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver
import org.ta4j.core.utils.TimeFrameMapping

/**
 * A rule for setting the minimum number of bars up to which an open position
 * should not be closed.
 *
 * Using this rule only makes sense for exit rules. For entry rules,
 * [isSatisfied] always returns `false`.
 *
 * @param barCount the minimum number of bars up to which an open position should not be closed
 * @param barSeries the bar series to determine bar duration from timeframe
 * @param runtimeContext the runtime context for accessing trading state
 */
class OpenedPositionMinimumBarCountRule(
    val barCount: Int,
    private val barSeries: BarSeries,
    private val runtimeContext: RuntimeContext,
) : AbstractRule() {

    init {
        require(barCount >= 1) { "Bar count must be positive" }
    }

    private val currentPositionResolver = RuntimeValueResolver<Position?> { context ->
        context.getValue(BackTestTradingRecord.TradingRecordContextKeys.CURRENT_POSITION) as? Position
    }

    private val entryTimeResolver = RuntimeValueResolver<Instant?> { context ->
        val position = context.getValue(BackTestTradingRecord.TradingRecordContextKeys.CURRENT_POSITION) as? Position
        position?.entry?.whenExecuted
    }

    override val isSatisfied: Boolean
        get() {
            val currentPosition = runtimeContext.getValue(currentPositionResolver)
            if (currentPosition?.isOpened != true) {
                val satisfied = false
                traceIsSatisfied(satisfied)
                return satisfied
            }

            val entryTime = runtimeContext.getValue(entryTimeResolver)
            if (entryTime == null) {
                val satisfied = false
                traceIsSatisfied(satisfied)
                return satisfied
            }

            val currentTime = runtimeContext.getValue(CurrentTimeResolver())
            if (currentTime == null) {
                val satisfied = false
                traceIsSatisfied(satisfied)
                return satisfied
            }

            // Get bar duration from timeframe using TimeFrameMapping
            val barDuration = TimeFrameMapping.getDuration(barSeries.timeFrame)

            // Calculate how many bars have passed since entry
            val timeSinceEntry = Duration.between(entryTime, currentTime)
            val barCountPassed = timeSinceEntry.toNanos() / barDuration.toNanos()

            val satisfied = barCountPassed.toInt() >= barCount
            traceIsSatisfied(satisfied)
            return satisfied
        }

    override fun toString(): String {
        return "OpenedPositionMinimumBarCountRule[barCount=$barCount] => $isSatisfied"
    }
}
