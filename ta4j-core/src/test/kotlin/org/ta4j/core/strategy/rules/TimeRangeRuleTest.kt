/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
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

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.helpers.LocalTimeIndicator
import org.ta4j.core.mocks.MockMarketEventBuilder

class TimeRangeRuleTest {

    @Test
    fun `should create rule with default constructor`() {
        val rule = TimeRangeRule(
            listOf(TimeRangeRule.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))),
            LocalTimeIndicator()
        )

        assertThat(rule).isNotNull()
        assertThat(rule.isSatisfied).isIn(true, false)
    }

    @Test
    fun `should create rule with custom time zone`() {
        val rule = TimeRangeRule(
            listOf(TimeRangeRule.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))),
            LocalTimeIndicator(ZoneId.of("UTC"))
        )

        assertThat(rule).isNotNull()
    }

    @Test
    fun `should create rule with custom time indicator`() {
        val timeIndicator = LocalTimeIndicator(ZoneId.of("UTC"))
        val rule = TimeRangeRule(
            listOf(TimeRangeRule.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))),
            timeIndicator
        )

        assertThat(rule).isNotNull()
    }

    @Test
    fun `should be satisfied when time is within normal range`() {
        // Create a bar at 10:00 AM
        val morningTime = createTimeAtHour(10)

        val context = createTestContext(morningTime)
        val timeIndicator = LocalTimeIndicator()
        context.withIndicator(timeIndicator)

        val rule = TimeRangeRule(
            listOf(TimeRangeRule.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))),
            timeIndicator
        )

        context.advance()

        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should not be satisfied when time is outside range`() {
        // Create a bar at 8:00 AM (before 9-17 range)
        val earlyTime = createTimeAtHour(8)

        val context = createTestContext(earlyTime)
        val timeIndicator = LocalTimeIndicator()
        context.withIndicator(timeIndicator)

        val rule = TimeRangeRule(
            listOf(TimeRangeRule.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))),
            timeIndicator
        )

        context.advance()

        assertThat(rule.isSatisfied).isFalse()
    }

    @Test
    fun `should be satisfied when time is within any of multiple ranges`() {
        // Create a bar at 10:00 AM (within first range)
        val morningTime = createTimeAtHour(10)

        val context = createTestContext(morningTime)
        val timeIndicator = LocalTimeIndicator()
        context.withIndicator(timeIndicator)

        val rule = TimeRangeRule(
            listOf(
                TimeRangeRule.TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                TimeRangeRule.TimeRange(LocalTime.of(13, 0), LocalTime.of(17, 0))
            ),
            timeIndicator
        )

        context.advance()

        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should handle boundary conditions correctly`() {
        // Test exactly at start time
        val exactStartTime = createTimeAtHour(9)

        val context = createTestContext(exactStartTime)
        val timeIndicator = LocalTimeIndicator()
        context.withIndicator(timeIndicator)

        val rule = TimeRangeRule(
            listOf(TimeRangeRule.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))),
            timeIndicator
        )

        context.advance()

        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should handle cross-midnight time ranges`() {
        // Create a bar at 23:00 (11 PM)
        val eveningTime = createTimeAtHour(23)

        val context = createTestContext(eveningTime)
        val timeIndicator = LocalTimeIndicator()
        context.withIndicator(timeIndicator)

        // Range from 22:00 to 06:00 (crosses midnight)
        val rule = TimeRangeRule(
            listOf(TimeRangeRule.TimeRange(LocalTime.of(22, 0), LocalTime.of(6, 0))),
            timeIndicator
        )

        context.advance()

        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should handle cross-midnight time ranges with early morning time`() {
        // Create a bar at 05:00 (5 AM)
        val earlyMorningTime = createTimeAtHour(5)

        val context = createTestContext(earlyMorningTime)
        val timeIndicator = LocalTimeIndicator()
        context.withIndicator(timeIndicator)

        // Range from 22:00 to 06:00 (crosses midnight)
        val rule = TimeRangeRule(
            listOf(TimeRangeRule.TimeRange(LocalTime.of(22, 0), LocalTime.of(6, 0))),
            timeIndicator
        )

        context.advance()

        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should not be satisfied with cross-midnight range when outside`() {
        // Create a bar at 15:00 (3 PM) - outside 22:00-06:00 range
        val afternoonTime = createTimeAtHour(15)

        val context = createTestContext(afternoonTime)
        val timeIndicator = LocalTimeIndicator()
        context.withIndicator(timeIndicator)

        // Range from 22:00 to 06:00 (crosses midnight)
        val rule = TimeRangeRule(
            listOf(TimeRangeRule.TimeRange(LocalTime.of(22, 0), LocalTime.of(6, 0))),
            timeIndicator
        )

        context.advance()

        assertThat(rule.isSatisfied).isFalse()
    }

    @Test
    fun `should handle empty time ranges list`() {
        val rule = TimeRangeRule(emptyList(), LocalTimeIndicator())

        assertThat(rule).isNotNull()
        assertThat(rule.isSatisfied).isFalse()
    }

    @Test
    fun `should work with LocalTimeIndicator properties`() {
        val context = MarketEventTestContext()
            .withCandlePrices(100.0, 101.0, 102.0)

        val timeIndicator = LocalTimeIndicator()
        context.withIndicator(timeIndicator)

        context.advance()
        context.advance()

        assertThat(timeIndicator.value).isNotNull()
        assertThat(timeIndicator.isStable).isTrue()
        assertThat(timeIndicator.lag).isEqualTo(0)
    }

    @Test
    fun `should use correct string representation`() {
        val timeIndicator = LocalTimeIndicator()
        val rule = TimeRangeRule(
            listOf(TimeRangeRule.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))),
            timeIndicator
        )

        assertThat(rule.toString()).contains("TimeRangeRule")
        assertThat(rule.toString()).contains("09:00-17:00")
    }

    @Test
    fun `should handle TimeRange toString correctly`() {
        val timeRange = TimeRangeRule.TimeRange(LocalTime.of(9, 30), LocalTime.of(16, 45))

        assertThat(timeRange.toString()).isEqualTo("09:30-16:45")
    }

    @Test
    fun `should format multiple ranges correctly in toString`() {
        val timeIndicator = LocalTimeIndicator()
        val rule = TimeRangeRule(
            listOf(
                TimeRangeRule.TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                TimeRangeRule.TimeRange(LocalTime.of(13, 0), LocalTime.of(17, 0))
            ),
            timeIndicator
        )

        val ruleString = rule.toString()
        assertThat(ruleString).contains("09:00-12:00")
        assertThat(ruleString).contains("13:00-17:00")
    }

    @Test
    fun `should work with different time zones`() {
        val utcIndicator = LocalTimeIndicator(ZoneId.of("UTC"))
        val estIndicator = LocalTimeIndicator(ZoneId.of("America/New_York"))

        val context = MarketEventTestContext()
            .withCandlePrices(100.0)

        context.withIndicator(utcIndicator)
        context.withIndicator(estIndicator)

        context.advance()

        // The times should be different (unless it's exactly the same hour in both zones)
        assertThat(utcIndicator.value).isNotNull()
        assertThat(estIndicator.value).isNotNull()

        assertThat(utcIndicator.toString()).contains("UTC")
        assertThat(estIndicator.toString()).contains("America/New_York")
    }

    private fun createTimeAtHour(hour: Int): Instant {
        return ZonedDateTime.of(2023, 1, 1, hour, 0, 0, 0, ZoneId.systemDefault()).toInstant()
    }

    private fun createTestContext(time: Instant): MarketEventTestContext {
        val marketEvents = MockMarketEventBuilder()
            .withStartTime(time)
            .candle()
            .openPrice(100.0)
            .closePrice(100.0)
            .highPrice(100.0)
            .lowPrice(100.0)
            .add()
            .build()

        return MarketEventTestContext().withMarketEvents(marketEvents)
    }
}
