/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.strategy.Rule

/**
 * A one-shot rule.
 *
 *
 *
 * Satisfied when the rule is satisfied for the first time, then never again.
 */
class JustOnceRule @JvmOverloads constructor(private val rule: Rule = BooleanRule.Companion.TRUE) : AbstractRule() {
    private var satisfied = false


    /**
     * Satisfied when the given `rule` is satisfied the first time, then never
     * again.
     *
     * @param rule the rule that should be satisfied only the first time
     */


    override val isSatisfied: Boolean
        get() {
            if (this.satisfied) {
                return false
            }

            this.satisfied = this.rule.isSatisfied
            return this.satisfied
        }
}
