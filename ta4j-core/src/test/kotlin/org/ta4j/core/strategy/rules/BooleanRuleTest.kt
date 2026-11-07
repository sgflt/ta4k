/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BooleanRuleTest {

    private lateinit var satisfiedRule: BooleanRule
    private lateinit var unsatisfiedRule: BooleanRule

    @BeforeEach
    fun setUp() {
        satisfiedRule = BooleanRule.TRUE
        unsatisfiedRule = BooleanRule.FALSE
    }

    @Test
    fun isSatisfied() {
        assertTrue(satisfiedRule.isSatisfied)
        assertTrue(satisfiedRule.isSatisfied)
        assertTrue(satisfiedRule.isSatisfied)
        assertTrue(satisfiedRule.isSatisfied)

        assertFalse(unsatisfiedRule.isSatisfied)
        assertFalse(unsatisfiedRule.isSatisfied)
        assertFalse(unsatisfiedRule.isSatisfied)
        assertFalse(unsatisfiedRule.isSatisfied)
    }
}