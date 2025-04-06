/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Lukáš Kvídera
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
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Sigma-Indicator (also called, "z-score" or "standard score").
 */
class SigmaIndicator(private val ref: NumericIndicator, barCount: Int) : NumericIndicator(ref.numFactory) {
    private val mean = ref.sma(barCount)
    private val sd = ref.stddev(barCount)


    private fun calculate(): Num {
        if (sd.value.isZero) {
            return numFactory.one()
        }

        // z-score = (ref - mean) / sd
        return ref.value.minus(mean.value).dividedBy(sd.value)
    }


    public override fun updateState(bar: Bar) {
        ref.onBar(bar)
        mean.onBar(bar)
        sd.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = ref.isStable && mean.isStable && sd.isStable
}
