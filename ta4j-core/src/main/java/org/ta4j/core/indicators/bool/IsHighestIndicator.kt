/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
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
 * is the highest within the `barCount`.
 */
class IsHighestIndicator(
    /** The actual indicator. */
    private val ref: NumericIndicator,
    /** The barCount. */
    private val barCount: Int,
) : BooleanIndicator() {

    /** Pre-computed highest value indicator for efficiency */
    private val highest = ref.highest(barCount)

    override fun updateState(bar: Bar) {
        ref.onBar(bar)
        highest.onBar(bar)

        val refVal = ref.value
        val highestVal = highest.value

        value = !refVal.isNaN && !highestVal.isNaN && refVal == highestVal
    }

    override val lag = highest.lag

    override val isStable: Boolean
        get() = ref.isStable && highest.isStable

    override fun toString(): String {
        return "IsHighest($ref, $barCount) => $value"
    }
}
