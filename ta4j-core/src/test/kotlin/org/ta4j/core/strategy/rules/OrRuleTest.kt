/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ta4j.core.strategy.Rule

internal class OrRuleTest {

    private lateinit var satisfiedRule: Rule
    private lateinit var unsatisfiedRule: Rule

    @BeforeEach
    fun setUp() {
        satisfiedRule = BooleanRule.TRUE
        unsatisfiedRule = BooleanRule.FALSE
    }

    @Test
    fun isSatisfied() {
        assertTrue(satisfiedRule.or(BooleanRule.FALSE).isSatisfied)
        assertTrue(BooleanRule.FALSE.or(satisfiedRule).isSatisfied)
        assertFalse(unsatisfiedRule.or(BooleanRule.FALSE).isSatisfied)
        assertFalse(BooleanRule.FALSE.or(unsatisfiedRule).isSatisfied)

        assertTrue(satisfiedRule.or(BooleanRule.TRUE).isSatisfied)
        assertTrue(BooleanRule.TRUE.or(satisfiedRule).isSatisfied)
        assertTrue(unsatisfiedRule.or(BooleanRule.TRUE).isSatisfied)
        assertTrue(BooleanRule.TRUE.or(unsatisfiedRule).isSatisfied)
    }
}