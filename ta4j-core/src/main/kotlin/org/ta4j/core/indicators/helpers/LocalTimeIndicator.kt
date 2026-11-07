/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import java.time.LocalTime
import java.time.ZoneId
import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar

/**
 * LocalTime indicator.
 *
 * Returns the local time of a bar's begin time in the specified time zone.
 */
class LocalTimeIndicator(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : Indicator<LocalTime> {

    override var value: LocalTime = LocalTime.MIDNIGHT
        private set

    override val lag = 0
    override val isStable = true

    override fun onBar(bar: Bar) {
        value = bar.beginTime.atZone(zoneId).toLocalTime()
    }

    override fun toString(): String {
        return "LocalTime(${zoneId.id}) => $value"
    }
}
