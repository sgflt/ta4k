package org.ta4j.core.utils

import org.ta4j.core.indicators.TimeFrame
import java.time.Duration

/**
 * @author Lukáš Kvídera
 */
object TimeFrameMapping {
    private val timeframeToDurationMapping = mapOf(
        TimeFrame.MINUTES_1 to Duration.ofSeconds(60),
        TimeFrame.MINUTES_5 to Duration.ofSeconds(5L * 60),
        TimeFrame.MINUTES_15 to Duration.ofSeconds(15L * 60),
        TimeFrame.MINUTES_30 to Duration.ofSeconds(30L * 60),
        TimeFrame.HOURS_1 to Duration.ofSeconds(60L * 60),
        TimeFrame.HOURS_4 to Duration.ofSeconds(4L * 60 * 60),
        TimeFrame.DAY to Duration.ofSeconds(24L * 60 * 60),
        TimeFrame.WEEK to Duration.ofSeconds(7 * 24L * 60 * 60),
        TimeFrame.MONTH to Duration.ofSeconds(30 * 24L * 60 * 60)
    )

    @JvmStatic
    fun getDuration(resolution: TimeFrame?) = timeframeToDurationMapping[resolution]
}
