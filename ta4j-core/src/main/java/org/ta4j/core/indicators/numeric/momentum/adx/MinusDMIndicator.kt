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
package org.ta4j.core.indicators.numeric.momentum.adx

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * -DM indicator.
 *
 * Part of the Directional Movement System.
 */
class MinusDMIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {
    private var previousBar: Bar? = null
    override var isStable = false
        private set

    override val lag: Int = 2

    private fun calculate(bar: Bar) = when {
        previousBar == null -> {
            previousBar = bar
            numFactory.zero()
        }

        else -> {
            isStable = true
            val prevBar = previousBar!!
            val upMove = bar.highPrice.minus(prevBar.highPrice)
            val downMove = prevBar.lowPrice.minus(bar.lowPrice)
            previousBar = bar

            if (downMove > upMove && downMove > numFactory.zero()) {
                downMove
            } else {
                numFactory.zero()
            }
        }
    }

    override fun updateState(bar: Bar) {
        value = calculate(bar)
    }
}
