/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.Num

class DateTimeIndicatorTest {

    @Test
    fun testEndTime() {
        val zoneId = ZoneId.of("UTC")
        val expectedZonedDateTime = ZonedDateTime.parse("2019-09-17T00:04:00Z", DateTimeFormatter.ISO_ZONED_DATE_TIME)

        val indicator = DateTimeIndicator(Bar::endTime, zoneId)

        val bar = object : Bar {
            override val timeFrame = TimeFrame.DAY
            override val timePeriod = Duration.ofDays(1)
            override val beginTime = expectedZonedDateTime.minusMinutes(1).toInstant()
            override val endTime = expectedZonedDateTime.toInstant()
            override val openPrice: Num get() = throw NotImplementedError()
            override val highPrice: Num get() = throw NotImplementedError()
            override val lowPrice: Num get() = throw NotImplementedError()
            override val closePrice: Num get() = throw NotImplementedError()
            override val volume: Num get() = throw NotImplementedError()
        }

        indicator.onBar(bar)

        assertThat(indicator.value).isEqualTo(expectedZonedDateTime)
    }

    @Test
    fun testBeginTime() {
        val zoneId = ZoneId.of("UTC")
        val expectedZonedDateTime = ZonedDateTime.parse("2019-09-17T00:03:00Z", DateTimeFormatter.ISO_ZONED_DATE_TIME)

        val indicator = DateTimeIndicator(zoneId = zoneId)

        val bar = object : Bar {
            override val timeFrame = TimeFrame.DAY
            override val timePeriod = Duration.ofDays(1)
            override val beginTime = expectedZonedDateTime.toInstant()
            override val endTime = expectedZonedDateTime.plusMinutes(1).toInstant()
            override val openPrice: Num get() = throw NotImplementedError()
            override val highPrice: Num get() = throw NotImplementedError()
            override val lowPrice: Num get() = throw NotImplementedError()
            override val closePrice: Num get() = throw NotImplementedError()
            override val volume: Num get() = throw NotImplementedError()
        }

        indicator.onBar(bar)

        assertThat(indicator.value).isEqualTo(expectedZonedDateTime)
    }

    @Test
    fun testStableProperty() {
        val indicator = DateTimeIndicator()
        assertThat(indicator.isStable).isTrue()
        assertThat(indicator.lag).isEqualTo(0)
    }
}
