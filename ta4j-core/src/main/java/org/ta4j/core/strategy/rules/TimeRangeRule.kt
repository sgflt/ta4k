/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
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

import java.time.LocalTime
import org.ta4j.core.indicators.helpers.LocalTimeIndicator

/**
 * Satisfied when the local time value is within the specified set of time ranges.
 *
 * This rule is useful for limiting trading to specific hours of the day,
 * such as market hours or avoiding low-liquidity periods.
 */
class TimeRangeRule(
    private val timeRanges: List<TimeRange>,
    private val timeIndicator: LocalTimeIndicator,
) : AbstractRule() {

    override val isSatisfied: Boolean
        get() {
            val localTime = timeIndicator.value
            val satisfied = timeRanges.any { timeRange ->
                localTime.isWithinRange(timeRange)
            }
            traceIsSatisfied(satisfied)
            return satisfied
        }

    override fun toString(): String {
        val rangesStr = timeRanges.joinToString(", ")
        return "TimeRangeRule[$rangesStr] => $isSatisfied"
    }

    /**
     * Represents a time range from one LocalTime to another.
     *
     * @param from the start time (inclusive)
     * @param to the end time (inclusive)
     */
    @JvmRecord
    data class TimeRange(
        val from: LocalTime,
        val to: LocalTime,
    ) {
        override fun toString(): String {
            return "$from-$to"
        }
    }

    private fun LocalTime.isWithinRange(timeRange: TimeRange): Boolean {
        return if (timeRange.from <= timeRange.to) {
            // Normal range (e.g., 09:00-17:00)
            !isBefore(timeRange.from) && !isAfter(timeRange.to)
        } else {
            // Cross-midnight range (e.g., 22:00-06:00)
            !isBefore(timeRange.from) || !isAfter(timeRange.to)
        }
    }
}
