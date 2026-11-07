/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import java.time.ZonedDateTime
import org.ta4j.core.indicators.helpers.DateTimeIndicator

/**
 * Satisfied when the hour of the bar time (0-23) is one of [hoursOfDay].
 *
 * @param timeIndicator the bar time, read in its time zone
 * @param hoursOfDay accepted hours
 */
class HourOfDayRule(
    timeIndicator: DateTimeIndicator,
    hoursOfDay: Collection<Int>,
) : AbstractTimeFieldRule<Int>(timeIndicator, hoursOfDay, "HourOfDayRule") {

    constructor(timeIndicator: DateTimeIndicator, vararg hoursOfDay: Int) : this(timeIndicator, hoursOfDay.toList())

    init {
        require(hoursOfDay.all { it in 0..23 }) { "Hours of day must be within 0..23, but were: $hoursOfDay" }
    }

    override fun field(dateTime: ZonedDateTime) = dateTime.hour
}
