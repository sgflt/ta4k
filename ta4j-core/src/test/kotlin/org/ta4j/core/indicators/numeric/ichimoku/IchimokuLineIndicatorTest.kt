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
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

class IchimokuLineIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testCalculation(numFactory: NumFactory) {
        val indicator = IchimokuLineIndicator(numFactory, 3)

        // Create mock bars with known high/low values
        val bars = listOf(
            createBar(numFactory, high = 10.0, low = 5.0),   // Expected: (10+5)/2 = 7.5
            createBar(
                numFactory,
                high = 12.0,
                low = 6.0
            ),   // Expected: (12+5)/2 = 8.5 (highest high: 12, lowest low: 5 from bars 0-1)
            createBar(
                numFactory,
                high = 8.0,
                low = 4.0
            ),    // Expected: (12+4)/2 = 8.0 (highest high: 12, lowest low: 4 from bars 0-2)
            createBar(
                numFactory,
                high = 15.0,
                low = 7.0
            ),   // Expected: (15+4)/2 = 9.5 (highest high: 15, lowest low: 4 from bars 1-3)
            createBar(
                numFactory,
                high = 11.0,
                low = 9.0
            )    // Expected: (15+7)/2 = 11.0 (highest high: 15, lowest low: 7 from bars 2-4)
        )

        // Process bars and check values
        for (bar in bars) {
            indicator.onBar(bar)
        }

        // After all bars processed, the value should be the average of highest high and lowest low over last 3 bars
        // Last 3 bars (indices 2,3,4): highs = [8,15,11], lows = [4,7,9]
        // Highest high = 15, lowest low = 4
        // Expected = (15 + 4) / 2 = 9.5
        assertNumEquals(9.5, indicator.value)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testLagAndStability(numFactory: NumFactory) {
        val barCount = 5
        val indicator = IchimokuLineIndicator(numFactory, barCount)

        assertThat(indicator.lag).isEqualTo(barCount)

        // Initially not stable (depends on child indicators)
        // Add some bars to make it stable
        repeat(barCount + 1) { i ->
            val bar = createBar(numFactory, high = 10.0 + i, low = 5.0 + i)
            indicator.onBar(bar)
        }

        assertThat(indicator.isStable).isTrue()
    }

    @Test
    fun testInitialValue() {
        val numFactory = DoubleNumFactory
        val indicator = IchimokuLineIndicator(numFactory, 3)

        // Before any bars, value should be NaN
        assertThat(indicator.value).isEqualTo(NaN)
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
