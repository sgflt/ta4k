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
package org.ta4j.core.indicators.numeric.helpers

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Calculates the sum of all indicator values.
 *
 * Sum = summand0 + summand1 + ... + summandN
 */
class SumIndicator(
    numFactory: NumFactory,
    private vararg val summands: NumericIndicator,
) : NumericIndicator(numFactory) {

    init {
        require(summands.isNotEmpty()) { "At least one summand is required" }
    }

    private fun calculate(): Num = summands.fold(numFactory.zero()) { sum, summand ->
        sum + summand.value
    }

    override fun updateState(bar: Bar) {
        summands.forEach { it.onBar(bar) }
        value = calculate()
    }

    override val lag: Int
        get() = summands.maxOfOrNull { it.lag } ?: 0

    override val isStable: Boolean
        get() = summands.all { it.isStable }

    override fun toString(): String {
        return "SUM(${summands.joinToString(", ")}) => $value"
    }
}
