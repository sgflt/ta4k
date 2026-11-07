/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.strategy.Rule

/**
 * Aggregates smaller rules.
 */
class CompoundRule(private val rules: List<Rule>) : Rule {
    /**
     * @return true only if all rules are satisfied
     */
    override val isSatisfied: Boolean
        get() = rules.all { it.isSatisfied }
}
