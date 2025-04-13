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
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Triple exponential moving average indicator (also called "TRIX").
 *
 *
 *
 * TEMA needs "3 * period - 2" of data to start producing values in contrast to
 * the period samples needed by a regular EMA.
 *
 * @see [https://en.wikipedia.org/wiki/Triple_exponential_moving_average](https://en.wikipedia.org/wiki/Triple_exponential_moving_average)
 *
 * @see [https://www.investopedia.com/terms/t/triple-exponential-moving-average.asp](https://www.investopedia.com/terms/t/triple-exponential-moving-average.asp)
 */
class TripleEMAIndicator(indicator: NumericIndicator, private val barCount: Int) :
    NumericIndicator(indicator.numFactory) {
    private val ema = EMAIndicator(indicator, barCount)
    private val emaEma = EMAIndicator(ema, barCount)
    private val emaEmaEma = EMAIndicator(emaEma, barCount)


    protected fun calculate(): Num {
        // trix = 3 * ( ema - emaEma ) + emaEmaEma
        return numFactory.numOf(3)
            .multipliedBy(ema.value.minus(emaEma.value))
            .plus(emaEmaEma.value)
    }


    public override fun updateState(bar: Bar) {
        ema.onBar(bar)
        emaEma.onBar(bar)
        emaEmaEma.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = ema.isStable && emaEma.isStable && emaEmaEma.isStable


    override fun toString() = "TrEMA($barCount) => $value"
}
