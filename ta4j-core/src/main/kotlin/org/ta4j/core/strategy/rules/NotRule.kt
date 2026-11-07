/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.strategy.Rule

/**
 * An opposite (logical operator: NOT) rule (i.e. a rule that is the negation of
 * another rule).
 *
 *
 *
 * Satisfied when the rule is not satisfied.<br></br>
 * Not satisfied when the rule is satisfied.
 */
class NotRule
/**
 * Constructor.
 *
 * @param ruleToNegate the trading rule to negate
 */(
    /** The trading rule to negate.  */
    val ruleToNegate: Rule,
) : AbstractRule() {
    /** @return [.ruleToNegate]
     */


    override val isSatisfied: Boolean
        get() {
            val satisfied = !this.ruleToNegate.isSatisfied
            traceIsSatisfied(satisfied)
            return satisfied
        }


    override fun toString(): String {
        return String.format("NotRule[%s] => %s", this.ruleToNegate, isSatisfied)
    }
}
