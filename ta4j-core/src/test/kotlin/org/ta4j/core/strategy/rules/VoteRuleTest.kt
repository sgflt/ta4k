/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.ta4j.core.strategy.Rule

class VoteRuleTest {

    private val yes = BooleanRule.TRUE
    private val no = BooleanRule.FALSE

    @Test
    fun `satisfied when enough rules vote yes`() {
        assertThat(Rule.vote(2, yes, no, yes).isSatisfied).isTrue()
        assertThat(Rule.vote(2, yes, no, no).isSatisfied).isFalse()
        assertThat(Rule.vote(3, yes, yes, yes).isSatisfied).isTrue()
        assertThat(Rule.vote(1, no, no, yes).isSatisfied).isTrue()
    }

    @Test
    fun `stops evaluating once enough votes are collected`() {
        var evaluated = false
        val spy = Rule.of { evaluated = true; true }

        assertThat(VoteRule(1, yes, spy).isSatisfied).isTrue()
        assertThat(evaluated).isFalse()
    }

    @Test
    fun `rejects invalid vote counts`() {
        assertThatThrownBy { VoteRule(0, yes) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { VoteRule(3, yes, no) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { VoteRule(1, emptyList()) }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `string representation shows vote threshold`() {
        assertThat(VoteRule(2, yes, no, yes).toString()).isEqualTo("VoteRule[2 of 3] => true")
    }
}
