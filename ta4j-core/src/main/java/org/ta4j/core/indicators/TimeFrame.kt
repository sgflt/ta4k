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
