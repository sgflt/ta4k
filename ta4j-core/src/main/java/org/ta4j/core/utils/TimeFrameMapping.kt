package org.ta4j.core.utils

import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.TimeFrames
import java.time.Duration

/**
 * @author Lukáš Kvídera
 */
object TimeFrameMapping {
    private val timeframeToDurationMapping = mapOf(
        TimeFrames.MINUTES_1 to Duration.ofSeconds(60),
        TimeFrames.MINUTES_5 to Duration.ofSeconds(5L * 60),
        TimeFrames.MINUTES_15 to Duration.ofSeconds(15L * 60),
        TimeFrames.MINUTES_30 to Duration.ofSeconds(30L * 60),
        TimeFrames.HOURS_1 to Duration.ofSeconds(60L * 60),
        TimeFrames.HOURS_4 to Duration.ofSeconds(4L * 60 * 60),
        TimeFrames.DAY to Duration.ofSeconds(24L * 60 * 60),
        TimeFrames.WEEK to Duration.ofSeconds(7 * 24L * 60 * 60),
        TimeFrames.MONTH to Duration.ofSeconds(30 * 24L * 60 * 60)
    )

    @JvmStatic
    fun getDuration(resolution: TimeFrame?) = timeframeToDurationMapping[resolution]
}
