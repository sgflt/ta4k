/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import java.time.ZonedDateTime
import org.ta4j.core.indicators.helpers.DateTimeIndicator

/**
 * Satisfied when the minute of the bar time (0-59) is one of [minutesOfHour].
 *
 * @param timeIndicator the bar time, read in its time zone
 * @param minutesOfHour accepted minutes
 */
class MinuteOfHourRule(
    timeIndicator: DateTimeIndicator,
    minutesOfHour: Collection<Int>,
) : AbstractTimeFieldRule<Int>(timeIndicator, minutesOfHour, "MinuteOfHourRule") {

    constructor(timeIndicator: DateTimeIndicator, vararg minutesOfHour: Int) : this(
        timeIndicator,
        minutesOfHour.toList()
    )

    init {
        require(minutesOfHour.all { it in 0..59 }) {
            "Minutes of hour must be within 0..59, but were: $minutesOfHour"
        }
    }

    override fun field(dateTime: ZonedDateTime) = dateTime.minute
}
