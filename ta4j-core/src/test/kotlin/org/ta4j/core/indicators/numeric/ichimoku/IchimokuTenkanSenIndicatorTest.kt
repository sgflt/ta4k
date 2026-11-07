/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.ichimoku

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

class IchimokuTenkanSenIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testDefaultBarCount(numFactory: NumFactory) {
        val indicator = IchimokuTenkanSenIndicator(numFactory)

        // Default bar count should be 9
        assertThat(indicator.lag).isEqualTo(9)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCustomBarCount(numFactory: NumFactory) {
        val customBarCount = 15
        val indicator = IchimokuTenkanSenIndicator(numFactory, customBarCount)

        assertThat(indicator.lag).isEqualTo(customBarCount)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCalculation(numFactory: NumFactory) {
        val indicator = IchimokuTenkanSenIndicator(numFactory, 3)

        // Create bars with known values
        val bars = listOf(
            createBar(numFactory, high = 20.0, low = 10.0),
            createBar(numFactory, high = 25.0, low = 15.0),
            createBar(numFactory, high = 30.0, low = 20.0)
        )

        for (bar in bars) {
            indicator.onBar(bar)
        }

        // Over last 3 bars: highest high = 30, lowest low = 10
        // Expected = (30 + 10) / 2 = 20
        assertNumEquals(20.0, indicator.value)
    }

    @Test
    fun testIsIchimokuLineIndicator() {
        val indicator = IchimokuTenkanSenIndicator(DoubleNumFactory)

        assertThat(indicator).isInstanceOf(IchimokuLineIndicator::class.java)
    }

    private fun createBar(numFactory: NumFactory, high: Double, low: Double): Bar {
        return object : Bar {
            override val timeFrame = TimeFrame.DAY
            override val timePeriod = Duration.ofDays(1)
            override val beginTime = Instant.now()
            override val endTime = Instant.now().plusSeconds(3600)
            override val openPrice: Num = numFactory.numOf((high + low) / 2)
            override val highPrice: Num = numFactory.numOf(high)
            override val lowPrice: Num = numFactory.numOf(low)
            override val closePrice: Num = numFactory.numOf((high + low) / 2)
            override val volume: Num = numFactory.numOf(1000)
        }
    }
}
