/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.helpers.DateTimeIndicator

class TimeFieldRulesTest {

    /** Daily candles at 09:15 UTC; the test builder places the first one a day after the start, on Monday 2026-11-02. */
    private fun context(timeIndicator: DateTimeIndicator) = MarketEventTestContext()
        .withStartTime(Instant.parse("2026-11-01T09:15:00Z"))
        .withCandlePrices(1.0, 2.0, 3.0, 4.0)
        .withIndicator(timeIndicator)

    @Test
    fun `hour of day rule matches hour in indicator time zone`() {
        val utc = DateTimeIndicator(zoneId = ZoneOffset.UTC)
        val prague = DateTimeIndicator(zoneId = ZoneId.of("Europe/Prague"))
        val context = context(utc).withIndicator(prague)

        context.advance()
        assertThat(utc.hourOfDayRule(9).isSatisfied).isTrue()
        assertThat(utc.hourOfDayRule(8, 10).isSatisfied).isFalse()
        assertThat(prague.hourOfDayRule(10).isSatisfied).isTrue()
    }

    @Test
    fun `minute of hour rule matches minute`() {
        val utc = DateTimeIndicator(zoneId = ZoneOffset.UTC)
        val context = context(utc)

        context.advance()
        assertThat(utc.minuteOfHourRule(0, 15, 30).isSatisfied).isTrue()
        assertThat(utc.minuteOfHourRule(16).isSatisfied).isFalse()
    }

    @Test
    fun `day of week rule matches day of each bar`() {
        val utc = DateTimeIndicator(zoneId = ZoneOffset.UTC)
        val context = context(utc)
        val rule = utc.dayOfWeekRule(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)

        context.advance() // Monday
        assertThat(rule.isSatisfied).isTrue()
        context.advance() // Tuesday
        assertThat(rule.isSatisfied).isFalse()
        context.advance() // Wednesday
        assertThat(rule.isSatisfied).isTrue()
        context.advance() // Thursday
        assertThat(rule.isSatisfied).isFalse()
        assertThat(rule.toString()).startsWith("DayOfWeekRule[MONDAY, WEDNESDAY]")
    }

    @Test
    fun `time field rules reject invalid values`() {
        val utc = DateTimeIndicator(zoneId = ZoneOffset.UTC)

        assertThatThrownBy { utc.hourOfDayRule(24) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { utc.minuteOfHourRule(-1) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { utc.hourOfDayRule() }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
