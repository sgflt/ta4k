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
class BollingerBandsUpperIndicator
/**
 * Constructor with `k` = 2.
 *
 * @param bbm the middle band Indicator. Typically an `SMAIndicator`
 * is used.
 * @param deviation the deviation above and below the middle, factored by k.
 * Typically a `StandardDeviationIndicator` is used.
 */
@JvmOverloads constructor(
    private val bbm: BollingerBandsMiddleIndicator,
    private val deviation: NumericIndicator,
    private val k: Num = deviation.numFactory.two(),
) : NumericIndicator(deviation.numFactory) {

    private fun calculate() = bbm.value + (deviation.value * k)

    override fun updateState(bar: Bar) {
        bbm.onBar(bar)
        deviation.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = bbm.isStable && deviation.isStable

    override val lag: Int
        get() = maxOf(bbm.lag, deviation.lag)

    override fun toString() = "BolBaUp(${bbm}, ${k}) => $value"
}
