/*
 * SPDX-License-Identifier: MIT
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
