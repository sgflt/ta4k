/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar

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

    override fun onBar(bar: Bar) {
        if (bar.beginTime.isAfter(currentBeginTime)) {
            value = action(bar).atZone(zoneId)
            currentBeginTime = bar.beginTime
        }
    }
}
