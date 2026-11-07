/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

/**
 * Satisfied when the rule has been initialized with `true`.
 */
class BooleanRule
/**
 * Constructor.
 *
 * @param satisfied true for the rule to be always satisfied, false to be never
 * satisfied
 */ private constructor(private val satisfied: Boolean) : AbstractRule() {
    override val isSatisfied: Boolean
        get() {
            traceIsSatisfied(satisfied)
            return satisfied
        }


    override fun toString(): String {
        return "BoolRule[$satisfied] => $isSatisfied"
    }

    companion object {
        /** An always-true rule.  */
        @JvmField
        val TRUE: BooleanRule = BooleanRule(true)

        /** An always-false rule.  */
        @JvmField
        val FALSE: BooleanRule = BooleanRule(false)
    }
}
