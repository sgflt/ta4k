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
package org.ta4j.core.indicators.bool

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Boolean indicator that returns true when the value of the [indicator][NumericIndicator]
 * is the lowest within the `barCount`.
 */
class IsLowestIndicator(
    /** The actual indicator. */
    private val ref: NumericIndicator,
    /** The barCount. */
    private val barCount: Int,
) : BooleanIndicator() {

    /** Pre-computed lowest value indicator for efficiency */
    private val lowest = ref.lowest(barCount)

    override fun updateState(bar: Bar) {
        ref.onBar(bar)
        lowest.onBar(bar)

        val refVal = ref.value
        val lowestVal = lowest.value

        value = !refVal.isNaN && !lowestVal.isNaN && refVal == lowestVal
    }

    override val lag = lowest.lag

    override val isStable: Boolean
        get() = ref.isStable && lowest.isStable

    override fun toString(): String {
        return "IsLowest($ref, $barCount) => $value"
    }
}
