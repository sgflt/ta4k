/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.strategy.rules.DayOfWeekRule
import org.ta4j.core.strategy.rules.HourOfDayRule
import org.ta4j.core.strategy.rules.MinuteOfHourRule

/**
 * DateTime indicator.
 *
 * Returns a [java.time.ZonedDateTime] of (or for) a bar.
 */
class DateTimeIndicator(
    private val action: (Bar) -> Instant = Bar::beginTime,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : Indicator<ZonedDateTime> {

    private var currentBeginTime: Instant = Instant.MIN
    override var value: ZonedDateTime = ZonedDateTime.now()
        private set

    override val lag: Int = 0

    override val isStable: Boolean = true

    /** @return [HourOfDayRule] satisfied when the bar hour is one of [hoursOfDay] */
    fun hourOfDayRule(vararg hoursOfDay: Int) = HourOfDayRule(this, *hoursOfDay)

    /** @return [MinuteOfHourRule] satisfied when the bar minute is one of [minutesOfHour] */
    fun minuteOfHourRule(vararg minutesOfHour: Int) = MinuteOfHourRule(this, *minutesOfHour)

    /** @return [DayOfWeekRule] satisfied when the bar day is one of [daysOfWeek] */
    fun dayOfWeekRule(vararg daysOfWeek: DayOfWeek) = DayOfWeekRule(this, *daysOfWeek)

    override fun onBar(bar: Bar) {
        if (bar.beginTime.isAfter(currentBeginTime)) {
            value = action(bar).atZone(zoneId)
            currentBeginTime = bar.beginTime
        }
    }
}
