/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.strategy

import java.util.function.BooleanSupplier
import org.ta4j.core.strategy.rules.AndRule
import org.ta4j.core.strategy.rules.BooleanRule
import org.ta4j.core.strategy.rules.NotRule
import org.ta4j.core.strategy.rules.OrRule
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

        fun of(rule: BooleanSupplier): Rule {
            return object : Rule {
                override val isSatisfied: Boolean
                    get() = rule.asBoolean
            }
        }
    }
}
