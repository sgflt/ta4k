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
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * A rule that monitors when one [indicator][Indicator] is over another.
 *
 *
 *
 * Satisfied when the value of the first [indicator][Indicator] is strictly
 * greater than the value of the second one.
 */
class OverIndicatorRule(
    /** The first indicator.  */
    private val first: NumericIndicator,
    /** The second indicator.  */
    private val second: NumericIndicator,
) : AbstractRule() {
    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param threshold the threshold
     */
    constructor(indicator: NumericIndicator, threshold: Number) : this(
        indicator,
        indicator.numFactory.numOf(threshold)
    )


    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param threshold the threshold
     */
    constructor(indicator: NumericIndicator, threshold: Num) : this(indicator, ConstantNumericIndicator(threshold))


    override val isSatisfied: Boolean
        get() {
            val satisfied = first.isGreaterThan(second)
            traceIsSatisfied(satisfied)
            return satisfied
        }


    override fun toString() = "OverRule[$first, $second] => ${isSatisfied}"
}
