/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.strategy.Rule

/**
 * A XOR combination of two [rules][Rule].
 *
 *
 *
 * Satisfied if only one of the two rules is satisfied. Not satisfied if no rule
 * or both rules are satisfied.
 */
class XorRule(
    /** @return the first rule
     */
    val rule1: Rule,
    /** @return the second rule
     */
    val rule2: Rule,
) : AbstractRule() {
    override val isSatisfied: Boolean
        get() {
            val satisfied = this.rule1.isSatisfied xor this.rule2.isSatisfied
            traceIsSatisfied(satisfied)
            return satisfied
        }


    override fun toString(): String {
        return String.format("XorRule[%s, %s] => %s", this.rule1, this.rule2, isSatisfied)
    }
}
