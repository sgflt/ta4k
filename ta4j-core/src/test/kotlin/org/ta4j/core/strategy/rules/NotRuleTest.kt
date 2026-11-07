/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ta4j.core.strategy.Rule

class NotRuleTest {

    private lateinit var satisfiedRule: Rule
    private lateinit var unsatisfiedRule: Rule

    @BeforeEach
    fun setUp() {
        satisfiedRule = BooleanRule.TRUE
        unsatisfiedRule = BooleanRule.FALSE
    }

    @Test
    fun isSatisfied() {
        assertFalse(satisfiedRule.negation().isSatisfied)
        assertTrue(unsatisfiedRule.negation().isSatisfied)

        assertFalse(satisfiedRule.negation().isSatisfied)
        assertTrue(unsatisfiedRule.negation().isSatisfied)
    }
}
