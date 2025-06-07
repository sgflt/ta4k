/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
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
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Satisfied when the value of the [NumericIndicator] is between two
 * other indicators or values (inclusive).
 *
 * The rule is satisfied when: lower <= ref <= upper
 */
class IsBetweenRule : AbstractRule {
    /** The upper indicator */
    private val upper: NumericIndicator

    /** The lower indicator */
    private val lower: NumericIndicator

    /** The evaluated indicator */
    private val ref: NumericIndicator

    /**
     * Constructor.
     *
     * @param ref   the reference indicator
     * @param upper the upper threshold
     * @param lower the lower threshold
     */
    constructor(ref: NumericIndicator, upper: Number, lower: Number) : this(
        ref,
        ref.numFactory.numOf(upper),
        ref.numFactory.numOf(lower)
    )

    /**
     * Constructor.
     *
     * @param ref   the reference indicator
     * @param upper the upper threshold
     * @param lower the lower threshold
     */
    constructor(ref: NumericIndicator, upper: Num, lower: Num) : this(
        ref,
        ConstantNumericIndicator(upper),
        ConstantNumericIndicator(lower)
    )

    /**
     * Constructor.
     *
     * @param ref   the reference indicator
     * @param upper the upper indicator
     * @param lower the lower indicator
     */
    constructor(ref: NumericIndicator, upper: NumericIndicator, lower: NumericIndicator) {
        this.upper = upper
        this.lower = lower
        this.ref = ref
    }

    override val isSatisfied: Boolean
        get() {
            val refValue = ref.value
            val satisfied = refValue <= upper.value && refValue >= lower.value
            traceIsSatisfied(satisfied)
            return satisfied
        }

    override fun toString(): String {
        return "IsBetweenRule[$ref, $lower, $upper] => $isSatisfied"
    }
}
