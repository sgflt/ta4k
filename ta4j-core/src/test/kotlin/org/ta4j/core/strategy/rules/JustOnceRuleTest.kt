/*
 * SPDX-License-Identifier: MIT
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