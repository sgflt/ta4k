/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ta4j.core.strategy.Rule

class AndRuleTest {

    private lateinit var satisfiedRule: Rule
    private lateinit var unsatisfiedRule: Rule

    @BeforeEach
    fun setUp() {
        satisfiedRule = BooleanRule.TRUE
        unsatisfiedRule = BooleanRule.FALSE
    }

    @Test
    fun isSatisfied() {
        assertFalse(satisfiedRule.and(BooleanRule.FALSE).isSatisfied)
        assertFalse(BooleanRule.FALSE.and(satisfiedRule).isSatisfied)
        assertFalse(unsatisfiedRule.and(BooleanRule.FALSE).isSatisfied)
        assertFalse(BooleanRule.FALSE.and(unsatisfiedRule).isSatisfied)

        assertTrue(satisfiedRule.and(BooleanRule.TRUE).isSatisfied)
        assertTrue(BooleanRule.TRUE.and(satisfiedRule).isSatisfied)
        assertFalse(unsatisfiedRule.and(BooleanRule.TRUE).isSatisfied)
        assertFalse(BooleanRule.TRUE.and(unsatisfiedRule).isSatisfied)
    }
}