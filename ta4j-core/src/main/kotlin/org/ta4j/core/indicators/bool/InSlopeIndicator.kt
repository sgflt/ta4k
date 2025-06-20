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

import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * A rule that monitors when an [Indicator] shows a specified slope.
 *
 *
 *
 * Satisfied when the difference of the value of the [indicator][Indicator]
 * and its previous (n-th) value is between the values of `maxSlope`
 * or/and `minSlope`. It can test both, positive and negative slope.
 */
class InSlopeIndicator(
    numFactory: NumFactory,
    ref: NumericIndicator,
    private val nthPrevious: Int,
    /** The minimum slope between ref and prev.  */
    private val minSlope: Num,
    /** The maximum slope between ref and prev.  */
    private val maxSlope: Num,
) : BooleanIndicator() {
    private val diff = ref.minus(PreviousNumericValueIndicator(numFactory, ref, nthPrevious))

    private fun calculate(): Boolean {
        val difference = diff.value
        val minSlopeSatisfied = minSlope.isNaN || difference >= minSlope
        val maxSlopeSatisfied = maxSlope.isNaN || difference <= maxSlope
        val isNaN = minSlope.isNaN && maxSlope.isNaN

        return minSlopeSatisfied && maxSlopeSatisfied && !isNaN
    }

    override fun updateState(bar: Bar) {
        diff.onBar(bar)
        value = calculate()
    }

    override val lag = nthPrevious

    override val isStable get() = diff.isStable
}
