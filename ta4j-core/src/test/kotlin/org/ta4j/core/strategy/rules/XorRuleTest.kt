/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ta4j.core.strategy.Rule

internal class XorRuleTest {

    private lateinit var satisfiedRule: Rule
    private lateinit var unsatisfiedRule: Rule

    @BeforeEach
    fun setUp() {
        satisfiedRule = BooleanRule.TRUE
        unsatisfiedRule = BooleanRule.FALSE
    }

    @Test
    fun isSatisfied() {
        assertTrue(satisfiedRule.xor(BooleanRule.FALSE).isSatisfied)
        assertTrue(BooleanRule.FALSE.xor(satisfiedRule).isSatisfied)
        assertFalse(unsatisfiedRule.xor(BooleanRule.FALSE).isSatisfied)
        assertFalse(BooleanRule.FALSE.xor(unsatisfiedRule).isSatisfied)

        assertFalse(satisfiedRule.xor(BooleanRule.TRUE).isSatisfied)
        assertFalse(BooleanRule.TRUE.xor(satisfiedRule).isSatisfied)
        assertTrue(unsatisfiedRule.xor(BooleanRule.TRUE).isSatisfied)
        assertTrue(BooleanRule.TRUE.xor(unsatisfiedRule).isSatisfied)
    }
}