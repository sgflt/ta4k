/*
 * SPDX-License-Identifier: MIT
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
