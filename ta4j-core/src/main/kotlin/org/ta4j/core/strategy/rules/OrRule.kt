/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.strategy.Rule

/**
 * An OR combination of two [rules][Rule].
 *
 *
 *
 * Satisfied when one of the two rules is satisfied. It doesn't matter which
 * one.
 *
 *
 *
 * **Warning:** The second rule is not tested if the first rule is satisfied.
 */
class OrRule(
    /** @return the first rule
     */
    val rule1: Rule,
    /** @return the second rule
     */
    val rule2: Rule,
) : AbstractRule() {
    override val isSatisfied: Boolean
        get() {
            val satisfied = this.rule1.isSatisfied || this.rule2.isSatisfied
            traceIsSatisfied(satisfied)
            return satisfied
        }


    override fun toString(): String {
        return String.format("OrRule[%s, %s] => %s", this.rule1, this.rule2, isSatisfied)
    }
}
