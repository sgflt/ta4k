/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.strategy.Rule

/**
 * Satisfied when at least [requiredVotes] of [rules] are satisfied.
 *
 * Evaluation stops as soon as enough rules are satisfied.
 *
 * @param requiredVotes minimum number of satisfied rules, within 1..rules.size
 * @param rules the voting rules
 */
class VoteRule(
    val requiredVotes: Int,
    val rules: List<Rule>,
) : AbstractRule() {

    constructor(requiredVotes: Int, vararg rules: Rule) : this(requiredVotes, rules.toList())

    init {
        require(rules.isNotEmpty()) { "At least one rule is required" }
        require(requiredVotes in 1..rules.size) {
            "requiredVotes must be within 1..${rules.size}, but was: $requiredVotes"
        }
    }

    override val isSatisfied: Boolean
        get() {
            var votes = 0
            for (rule in rules) {
                if (rule.isSatisfied && ++votes >= requiredVotes) {
                    break
                }
            }
            val satisfied = votes >= requiredVotes
            traceIsSatisfied(satisfied)
            return satisfied
        }

    override fun toString() = "VoteRule[$requiredVotes of ${rules.size}] => $isSatisfied"
}
