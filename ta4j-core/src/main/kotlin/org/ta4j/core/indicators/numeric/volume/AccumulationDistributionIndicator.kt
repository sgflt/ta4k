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
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.CloseLocationValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Accumulation-distribution indicator.
 *
 * The Accumulation/Distribution Line is a volume-based indicator designed to measure
 * the cumulative flow of money into and out of a security. It uses the Close Location
 * Value (CLV) to determine whether money is flowing into or out of a security.
 *
 * Formula:
 * 1. Money Flow Multiplier = Close Location Value (CLV)
 * 2. Money Flow Volume = Money Flow Multiplier × Volume
 * 3. AD = Previous AD + Money Flow Volume
 */
class AccumulationDistributionIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {

    private val clvIndicator = CloseLocationValueIndicator(numFactory)
    private var accumulationDistribution = numFactory.zero()

    private fun calculate(bar: Bar): Num {
        val moneyFlowMultiplier = clvIndicator.value
        val moneyFlowVolume = moneyFlowMultiplier * bar.volume
        accumulationDistribution += moneyFlowVolume
        return accumulationDistribution
    }

    override fun updateState(bar: Bar) {
        clvIndicator.onBar(bar)

        value = calculate(bar)
    }

    override val lag = 0

    override val isStable = true
}
