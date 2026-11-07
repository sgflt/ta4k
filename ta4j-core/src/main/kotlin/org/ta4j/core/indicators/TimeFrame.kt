/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators

/**
 * @param name name of timeframe. f.e.: 1m, 2m 5m, 1h, 4h, 1d or any custom
 */
@JvmInline
value class TimeFrame(val name: String) {

    companion object {
        val UNDEFINED = TimeFrame("UNDEFINED")

        val MINUTES_1 = TimeFrame("1m")

        val MINUTES_5 = TimeFrame("5m")

        val MINUTES_15 = TimeFrame("15m")

        val MINUTES_30 = TimeFrame("30m")

        val HOURS_1 = TimeFrame("1h")

        val HOURS_4 = TimeFrame("4h")

        val DAY = TimeFrame("1d")

        val WEEK = TimeFrame("1w")

        val MONTH = TimeFrame("1M")
    }
}
