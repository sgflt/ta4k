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
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Buy - Occurs when the price line crosses from below to above the Lower
 * Bollinger Band.
 *
 *
 *
 * Sell - Occurs when the price line crosses from above to below the Upper
 * Bollinger Band.
 */
class BollingerBandsLowerIndicator @JvmOverloads constructor(
    private val bbm: BollingerBandsMiddleIndicator,
    private val indicator: NumericIndicator,
    private val k: Num = indicator.numFactory.two(),
) : NumericIndicator(indicator.numFactory) {

    private fun calculate() = bbm.value.minus(indicator.value.multipliedBy(k))

    override fun updateState(bar: Bar) {
        bbm.onBar(bar)
        indicator.onBar(bar)
        value = calculate()
    }

    override val isStable
        get() = indicator.isStable && bbm.isStable

    override fun toString() = "BolBaLo(${bbm}, ${k}) => $value"
}
