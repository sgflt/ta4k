/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.aggregator

import java.time.Duration
import java.time.Instant
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactoryProvider

class BarAggregatorTest {

    private lateinit var barAggregator: BarAggregator
    private lateinit var mockListener: BarListener
    private val timeframe = TimeFrame.MINUTES_1

    @BeforeEach
    fun setup() {
        mockListener = mockk(relaxed = true)
        barAggregator = BarAggregator(setOf(timeframe))
        barAggregator.addBarListener(mockListener)
    }

    @Test
    fun aggregatesSingleBarCorrectly() {
        val bar = mockk<Bar> {
            every { endTime } returns Instant.now()
            every { openPrice } returns mockk()
            every { highPrice } returns mockk()
            every { lowPrice } returns mockk()
            every { closePrice } returns mockk()
            every { volume } returns mockk()
        }

        barAggregator.onBar(bar)

        verify(exactly = 0) { mockListener.onBar(any()) }
    }

    @Test
    fun handlesEmptyTimeframesGracefully() {
        val emptyAggregator = BarAggregator(emptySet())
        val bar = mockk<Bar>()

        emptyAggregator.onBar(bar)

        confirmVerified(mockListener)
    }

    @Test
    fun `Aggregate 5minutes bar from 1 minute bars`() {
        val fiveMinTimeframe = TimeFrame.MINUTES_5
        val listener = mockk<BarListener>(relaxed = true)
        val aggregator = BarAggregator(setOf(fiveMinTimeframe))
        aggregator.addBarListener(listener)
        val numFactory = NumFactoryProvider.defaultNumFactory
        val baseTime = Instant.parse("2024-01-01T00:00:00Z")
        val bars = (0 until 5).map { i ->
            mockk<Bar> {
                every { beginTime } returns baseTime.plus(Duration.ofMinutes(i.toLong()))
                every { endTime } returns beginTime.plus(Duration.ofMinutes(1))
                every { openPrice } returns numFactory.numOf(100 + i)
                every { highPrice } returns numFactory.numOf(110 + i)
                every { lowPrice } returns numFactory.numOf(90 + i)
                every { closePrice } returns numFactory.numOf(105 + i)
                every { volume } returns numFactory.numOf(1000 + i * 10)
            }
        }

        bars.forEach { aggregator.onBar(it) }

        verify(exactly = 1) {
            listener.onBar(match {
                it.endTime == baseTime.plus(Duration.ofMinutes(5))
                        && it.openPrice == numFactory.numOf(100)
                        && it.highPrice == numFactory.numOf(114)
                        && it.lowPrice == numFactory.numOf(90)
                        && it.closePrice == numFactory.numOf(109)
                        && it.volume == numFactory.numOf(5100)
            })
        }
    }

    @Test
    fun `Aggregate 15minutes bar from 1 minute bars`() {
        val fifteenMinuteTimeframe = TimeFrame.MINUTES_15
        val listener = mockk<BarListener>(relaxed = true)
        val aggregator = BarAggregator(setOf(fifteenMinuteTimeframe))
        aggregator.addBarListener(listener)
        val numFactory = NumFactoryProvider.defaultNumFactory
        val baseTime = Instant.parse("2024-01-01T00:00:00Z")
        val bars = (0 until 15).map { i ->
            mockk<Bar> {
                every { beginTime } returns baseTime.plus(Duration.ofMinutes(i.toLong()))
                every { endTime } returns beginTime.plus(Duration.ofMinutes(1))
                every { openPrice } returns numFactory.numOf(100 + i)
                every { highPrice } returns numFactory.numOf(110 + i)
                every { lowPrice } returns numFactory.numOf(90 + i)
                every { closePrice } returns numFactory.numOf(105 + i)
                every { volume } returns numFactory.numOf(1000 + i * 10)
            }
        }

        bars.forEach { aggregator.onBar(it) }

        verify(exactly = 1) {
            listener.onBar(match {
                it.endTime == baseTime.plus(Duration.ofMinutes(15))
                        && it.openPrice == numFactory.numOf(100)
                        && it.highPrice == numFactory.numOf(124)
                        && it.lowPrice == numFactory.numOf(90)
                        && it.closePrice == numFactory.numOf(119)
                        && it.volume == numFactory.numOf(16050)
            })
        }
    }

    @Test
    fun `Aggregate 5 and 15 minutes`() {
        val listener = mockk<BarListener>(relaxed = true)
        val aggregator = BarAggregator(setOf(TimeFrame.MINUTES_15, TimeFrame.MINUTES_5))
        aggregator.addBarListener(listener)
        val numFactory = NumFactoryProvider.defaultNumFactory
        val baseTime = Instant.parse("2024-01-01T00:00:00Z")
        val bars = (0 until 15).map { i ->
            mockk<Bar> {
                every { beginTime } returns baseTime.plus(Duration.ofMinutes(i.toLong()))
                every { endTime } returns beginTime.plus(Duration.ofMinutes(1))
                every { openPrice } returns numFactory.numOf(100 + i)
                every { highPrice } returns numFactory.numOf(110 + i)
                every { lowPrice } returns numFactory.numOf(90 + i)
                every { closePrice } returns numFactory.numOf(105 + i)
                every { volume } returns numFactory.numOf(1000 + i * 10)
            }
        }

        bars.forEach { aggregator.onBar(it) }

        verify(exactly = 4) { listener.onBar(any()) }
    }
}
