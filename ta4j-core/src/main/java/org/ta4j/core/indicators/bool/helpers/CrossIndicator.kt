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
package org.ta4j.core.indicators.bool.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Cross indicator.
 *
 * Boolean indicator that monitors the crossing of two indicators.
 */
class CrossIndicator(
    val up: NumericIndicator,
    val low: NumericIndicator,
    barCount: Int,
) : BooleanIndicator() {

    private val previousUp: PreviousNumericValueIndicator = up.previous(barCount)
    private val previousLow: PreviousNumericValueIndicator = low.previous(barCount)

    private fun calculate() = up.value > low.value && previousUp.value <= previousLow.value

    override fun updateState(bar: Bar) {
        low.onBar(bar)
        up.onBar(bar)
        previousUp.onBar(bar)
        previousLow.onBar(bar)
        value = calculate()
    }

    override val lag
        get() = previousUp.lag

    override val isStable
        get() = up.isStable && low.isStable && previousUp.isStable && previousLow.isStable

    override fun toString() =
        "Cross($up, $low, $previousUp, $previousLow) => $value"
}

