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
        return String.format("UnderRule[%s, %s] => %s", this.rule1, this.rule2, isSatisfied)
    }
}
