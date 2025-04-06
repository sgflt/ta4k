/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.api.series

import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.api.Indicators.openPrice
import org.ta4j.core.num.Num
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * A `Bar` is aggregated open/high/low/close/volume/etc. data over a time
 * period. It represents the "end bar" of a time period.
 */
interface Bar {
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
        get() {
            val openPrice = openPrice()
            val closePrice = closePrice()
            return closePrice.isLessThan(openPrice)
        }

    val isBullish: Boolean
        /**
         * @return true if this is a bullish bar, false otherwise
         */
        get() {
            val openPrice = openPrice()
            val closePrice = closePrice()
            return openPrice.isLessThan(closePrice)
        }
}
