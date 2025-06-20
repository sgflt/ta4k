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
