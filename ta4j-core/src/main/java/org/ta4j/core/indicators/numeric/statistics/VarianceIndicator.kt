/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Variance indicator.
 */
class VarianceIndicator(private val indicator: NumericIndicator, private val barCount: Int) :
    NumericIndicator(indicator.numFactory) {
    private val divisor: Num = numFactory.numOf(barCount - 1)
    private val oldestValue = indicator.previous(barCount)
    private var mean = numFactory.zero()
    private var currentIndex = 0
    private var _value = numFactory.zero()

    init {
        require(barCount > 1) { "barCount must be greater than 1" }
    }

    private fun calculate(): Num {
        if (currentIndex < barCount) {
            return add(indicator.value)
        }

        val oldValue = oldestValue.value
        return dropOldestAndAddNew(oldValue, indicator.value)
    }

    fun add(x: Num): Num {
        currentIndex++
        val delta = x - mean
        mean += delta / numFactory.numOf(currentIndex)
        return _value + delta * (x - mean)
    }

    private fun dropOldestAndAddNew(x: Num, y: Num): Num {
        val deltaYX = y - x
        val deltaX = x - mean
        val deltaY = y - mean
        mean += deltaYX / numFactory.numOf(barCount)
        val deltaYp = y - mean
        return _value - (numFactory.numOf(barCount) * (deltaX * deltaX - deltaY * deltaYp) / divisor) -
                (deltaYX * deltaYp / divisor)
    }

    override var value: Num
        get() = _value / divisor
        protected set(value) {
            _value = value
        }

    public override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        oldestValue.onBar(bar)
        value = calculate()
    }

    override val isStable: Boolean
        get() = currentIndex >= barCount && indicator.isStable

    override val lag: Int
        get() = barCount

    override fun toString() = "VAR($indicator, $barCount) => $value"
}
