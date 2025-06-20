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

package org.ta4j.core.utils

import java.time.Duration
import org.ta4j.core.indicators.TimeFrame

/**
 * @author Lukáš Kvídera
 */
object TimeFrameMapping {
    @JvmStatic
    fun getDuration(timeFrame: TimeFrame): Duration = when (timeFrame) {
        TimeFrame.MINUTES_1 -> Duration.ofSeconds(60)
        TimeFrame.MINUTES_5 -> Duration.ofSeconds(5L * 60)
        TimeFrame.MINUTES_15 -> Duration.ofSeconds(15L * 60)
        TimeFrame.MINUTES_30 -> Duration.ofSeconds(30L * 60)
        TimeFrame.HOURS_1 -> Duration.ofSeconds(60L * 60)
        TimeFrame.HOURS_4 -> Duration.ofSeconds(4L * 60 * 60)
        TimeFrame.DAY -> Duration.ofSeconds(24L * 60 * 60)
        TimeFrame.WEEK -> Duration.ofSeconds(7 * 24L * 60 * 60)
        TimeFrame.MONTH -> Duration.ofSeconds(30 * 24L * 60 * 60)
        else -> throw IllegalArgumentException("Unsupported time frame: $timeFrame")
    }

    @JvmStatic
    fun getTimeFrame(duration: Duration): TimeFrame = when (duration) {
        Duration.ofSeconds(60) -> TimeFrame.MINUTES_1
        Duration.ofSeconds(5L * 60) -> TimeFrame.MINUTES_5
        Duration.ofSeconds(15L * 60) -> TimeFrame.MINUTES_15
        Duration.ofSeconds(30L * 60) -> TimeFrame.MINUTES_30
        Duration.ofSeconds(60L * 60) -> TimeFrame.HOURS_1
        Duration.ofSeconds(4L * 60 * 60) -> TimeFrame.HOURS_4
        Duration.ofSeconds(24L * 60 * 60) -> TimeFrame.DAY
        Duration.ofSeconds(7 * 24L * 60 * 60) -> TimeFrame.WEEK
        Duration.ofSeconds(30 * 24L * 60 * 60) -> TimeFrame.MONTH
        else -> throw IllegalArgumentException("Unsupported duration: $duration")
    }
}
