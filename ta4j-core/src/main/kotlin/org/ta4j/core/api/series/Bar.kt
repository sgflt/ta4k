/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.api.series

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.Num

/**
 * A `Bar` is aggregated open/high/low/close/volume/etc. data over a time
 * period. It represents the "end bar" of a time period.
 */
interface Bar {

    /**
     * @return the time frame of the bar
     */
    val timeFrame: TimeFrame

    /**
     * @return the time period of the bar
     */
    val timePeriod: Duration

    /**
     * @return the begin timestamp of the bar period
     */
    val beginTime: Instant

    /**
     * @return the end timestamp of the bar period
     */
    val endTime: Instant

    /**
     * @return the open price of the bar period
     */
    val openPrice: Num

    /**
     * @return the high price of the bar period
     */
    val highPrice: Num

    /**
     * @return the low price of the bar period
     */
    val lowPrice: Num

    /**
     * @return the close price of the bar period
     */
    val closePrice: Num

    /**
     * @return the total traded volume of the bar period
     */
    val volume: Num

    /**
     * @param timestamp a timestamp
     *
     * @return true if the provided timestamp is between the begin time and the end
     * time of the current period, false otherwise
     */
    fun inPeriod(timestamp: Instant?): Boolean {
        return timestamp != null && !timestamp.isBefore(beginTime) && timestamp.isBefore(endTime)
    }

    val dateName: String
        /**
         * @return a human-friendly string of the end timestamp
         */
        get() = endTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME)

    val simpleDateName: String
        /**
         * @return a even more human-friendly string of the end timestamp
         */
        get() = endTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    val isBearish: Boolean
        /**
         * @return true if this is a bearish bar, false otherwise
         */
        get() = closePrice < openPrice

    val isBullish: Boolean
        /**
         * @return true if this is a bullish bar, false otherwise
         */
        get() = openPrice < closePrice
}
