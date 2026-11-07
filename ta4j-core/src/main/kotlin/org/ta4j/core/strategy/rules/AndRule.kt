/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.strategy.Rule

/**
 * An AND combination of two [rules][Rule].
 *
 *
 *
 * Satisfied when both rules are satisfied.
 *
 *
 *
 * **Warning:** The second rule is not tested if the first rule is not
 * satisfied.
 */
class AndRule
/**
 * Constructor.
 *
 * @param rule1 a trading rule
 * @param rule2 another trading rule
 */(
    /** @return the first rule
     */
    val rule1: Rule,
    /** @return the second rule
     */
    val rule2: Rule,
) : AbstractRule() {
    override val isSatisfied: Boolean
        get() {
            val satisfied = rule1.isSatisfied && rule2.isSatisfied
            traceIsSatisfied(satisfied)
            return satisfied
        }


    override fun toString(): String {
        return String.format("AndRule[%s, %s] => %s", this.rule1, this.rule2, isSatisfied)
    }
}
