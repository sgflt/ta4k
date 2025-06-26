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
package org.ta4j.core.indicators.numeric.average

import kotlin.math.ceil
import kotlin.math.floor
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Asymmetric Triangular Moving Average (ATMA) Indicator.
 *
 * ATMA is a double-smoothing of the Simple Moving Average (SMA), however unlike
 * the Triangular Moving Average (TMA) the modified equation creates a smoother
 * and slightly lagged moving average compared to the traditional TMA.
 */
class ATMAIndicator(
    indicator: NumericIndicator,
    private val barCount: Int,
) : NumericIndicator(indicator.numFactory) {

    private val fast = ceil(barCount / 2.0).toInt()
    private val slow = (floor(barCount / 2.0) + 1).toInt()
    private val sma = indicator.sma(fast)
    private val smaSma = sma.sma(slow)
    private var barsPassed = 0

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = barsPassed >= fast + slow

    override fun updateState(bar: Bar) {
        sma.onBar(bar)
        smaSma.onBar(bar)

        value = smaSma.value
        ++barsPassed
    }

    override fun toString(): String = "ATMA($barCount) => $value"
}
