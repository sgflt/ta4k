/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import java.time.DayOfWeek
import java.time.ZonedDateTime
import org.ta4j.core.indicators.helpers.DateTimeIndicator

/**
 * Satisfied when the day of week of the bar time is one of [daysOfWeek].
 *
 * @param timeIndicator the bar time, read in its time zone
 * @param daysOfWeek accepted days
 */
class DayOfWeekRule(
    timeIndicator: DateTimeIndicator,
    daysOfWeek: Collection<DayOfWeek>,
) : AbstractTimeFieldRule<DayOfWeek>(timeIndicator, daysOfWeek, "DayOfWeekRule") {

    constructor(timeIndicator: DateTimeIndicator, vararg daysOfWeek: DayOfWeek) : this(
        timeIndicator,
        daysOfWeek.toList()
    )

    override fun field(dateTime: ZonedDateTime): DayOfWeek = dateTime.dayOfWeek
}
