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
