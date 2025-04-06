/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
