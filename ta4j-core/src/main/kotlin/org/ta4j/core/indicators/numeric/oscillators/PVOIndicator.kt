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
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.numeric.average.PPOIndicator
import org.ta4j.core.num.NumFactory

/**
 * Percentage Volume Oscillator (PVO) indicator.
 *
 * <pre>
 * ((12-day EMA of Volume - 26-day EMA of Volume) / 26-day EMA of Volume) x 100
</pre> *
 *
 * @see [
 * https://school.stockcharts.com/doku.php?id=technical_indicators:percentage_volume_oscillator_pvo
](https://school.stockcharts.com/doku.php?id=technical_indicators:percentage_volume_oscillator_pvo) *
 */
class PVOIndicator(
    numFactory: NumFactory,
    volumeBarCount: Int,
    shortBarCount: Int = 12,
    longBarCount: Int = 26,
) : PPOIndicator(
    numFactory = numFactory,
    indicator = Indicators.extended(numFactory).volume().runningTotal(volumeBarCount),
    shortBarCount = shortBarCount,
    longBarCount = longBarCount,
)
