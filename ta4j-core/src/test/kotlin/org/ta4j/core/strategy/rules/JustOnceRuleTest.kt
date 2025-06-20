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

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ta4j.core.strategy.Rule

internal class JustOnceRuleTest {

    private lateinit var rule: JustOnceRule

    @BeforeEach
    fun setUp() {
        rule = JustOnceRule()
    }

    @Test
    fun isSatisfied() {
        assertTrue(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
    }

    @Test
    fun isSatisfiedWithInnerSatisfiedRule() {
        val rule = JustOnceRule(BooleanRule.TRUE)
        assertTrue(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
    }

    @Test
    fun isNotSatisfiedWithInnerNonSatisfiedRule() {
        val rule = JustOnceRule(BooleanRule.FALSE)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
    }

    @Test
    fun isSatisfiedWithInnerRule() {
        val rule = JustOnceRule(object : Rule {
            private var counter = 0

            override val isSatisfied: Boolean
                get() = counter++ > 0

            override fun negation(): Rule {
                return NotRule(this)
            }

            override fun xor(rule: Rule): Rule {
                return XorRule(this, rule)
            }

            override fun and(rule: Rule): Rule {
                return AndRule(this, rule)
            }

            override fun or(rule: Rule): Rule {
                return OrRule(this, rule)
            }
        })
        assertFalse(rule.isSatisfied)
        assertTrue(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
        assertFalse(rule.isSatisfied)
    }
}