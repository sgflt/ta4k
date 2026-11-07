/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy

import org.ta4j.core.TradeType
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.strategy.rules.AndRule
import org.ta4j.core.strategy.rules.BooleanRule
import org.ta4j.core.strategy.rules.LossTriggeredCooldownRule
import org.ta4j.core.strategy.rules.NotRule
import org.ta4j.core.strategy.rules.OrRule
import org.ta4j.core.strategy.rules.VoteRule
import org.ta4j.core.strategy.rules.XorRule

/**
 * A rule (also called "trading rule") used to build a [trading][Strategy]. A trading rule can consist of a combination of other rules.
 */
interface Rule {
    /**
     * @param rule another trading rule
     *
     * @return a rule which is the AND combination of this rule with the provided
     * one
     */
    infix fun and(rule: Rule): Rule {
        return AndRule(this, rule)
    }

    /**
     * @param rule another trading rule
     *
     * @return a rule which is the OR combination of this rule with the provided one
     */
    infix fun or(rule: Rule): Rule {
        return OrRule(this, rule)
    }

    /**
     * @param rule another trading rule
     *
     * @return a rule which is the XOR combination of this rule with the provided
     * one
     */
    infix fun xor(rule: Rule): Rule {
        return XorRule(this, rule)
    }

    /**
     * @return a rule which is the logical negation of this rule
     */
    fun negation(): Rule {
        return NotRule(this)
    }

    /**
     * @return true if this rule is satisfied for the provided index, false
     * otherwise
     */
    val isSatisfied: Boolean

    companion object {
        val NOOP: Rule = BooleanRule.FALSE

        /**
         * @return rule satisfied when at least [requiredVotes] of [rules] are satisfied
         */
        fun vote(requiredVotes: Int, vararg rules: Rule): Rule = VoteRule(requiredVotes, *rules)

        /**
         * @return rule blocking entries for [cooldownBars] bars of [timeFrame] after a losing position
         */
        fun cooldownAfterLoss(
            cooldownBars: Int,
            timeFrame: TimeFrame,
            runtimeContext: RuntimeContext,
            resetRule: Rule? = null,
            tradeType: TradeType? = null,
        ): Rule = LossTriggeredCooldownRule(cooldownBars, timeFrame, runtimeContext, resetRule, tradeType)

        fun of(rule: () -> Boolean): Rule {
            return object : Rule {
                override val isSatisfied: Boolean
                    get() = rule()
            }
        }
    }
}
