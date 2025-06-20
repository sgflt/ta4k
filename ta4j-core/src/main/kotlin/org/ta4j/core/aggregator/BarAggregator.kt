/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.aggregator

import java.time.Duration
import java.time.Instant
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.Num
import org.ta4j.core.utils.TimeFrameMapping

data class AggregatedBar(
    override val timeFrame: TimeFrame,
    override val timePeriod: Duration,
    override val beginTime: Instant,
    override val endTime: Instant,
    override val openPrice: Num,
    override val highPrice: Num,
    override val lowPrice: Num,
    override val closePrice: Num,
    override val volume: Num,
) : Bar

/**
 * Aggregates a list of [bars][Bar] into another one.
 */
class BarAggregator(private val timeframes: Set<TimeFrame>) : BarListener {

    private val listeners = mutableListOf<BarListener>()
    private val currentAggregations = mutableMapOf<TimeFrame, AggregatedBar?>()

    init {
        timeframes.forEach { timeframe ->
            currentAggregations[timeframe] = null
        }
    }

    /**
     * Adds a listener to be notified when an aggregated bar is produced.
     */
    fun addBarListener(listener: BarListener) {
        listeners += listener
    }

    override fun onBar(bar: Bar) {
        val completedBars = mutableListOf<Bar>()

        // Process each timeframe
        timeframes.forEach { timeframe ->
            val timeframeBoundary = calculateTimeframeBoundary(bar.endTime, timeframe)
            val currentAggregation = currentAggregations[timeframe]

            if (currentAggregation == null) {
                // First bar for this timeframe
                currentAggregations[timeframe] = createInitialAggregatedBar(bar, timeframe, timeframeBoundary)
            } else {
                // Check if the new bar belongs to the current aggregation or a new one
                if (bar.isLastInBucket(timeframe)) {
                    // Complete the current aggregation and start a new one
                    completedBars += updateAggregatedBar(currentAggregation, bar)
                    currentAggregations[timeframe] = createInitialAggregatedBar(bar, timeframe, timeframeBoundary)
                } else {
                    // Update the current aggregation
                    currentAggregations[timeframe] = updateAggregatedBar(currentAggregation, bar)
                }
            }
        }

        // Notify listeners of completed bars
        completedBars.forEach { completedBar ->
            notifyListeners(completedBar)
        }
    }

    /**
     * Calculates the timeframe boundary (start time) for a given timestamp and duration.
     */
    private fun calculateTimeframeBoundary(timestamp: Instant, timeframe: TimeFrame): Instant {
        val epochMillis = timestamp.toEpochMilli()
        val timeframeMillis = TimeFrameMapping.getDuration(timeframe).toMillis()
        val boundaryMillis = epochMillis - (epochMillis % timeframeMillis)
        return Instant.ofEpochMilli(boundaryMillis)
    }

    private fun Bar.isLastInBucket(timeFrame: TimeFrame): Boolean {
        val thisBucket = calculateTimeframeBoundary(beginTime, timeFrame)
        val nextBucket =
            calculateTimeframeBoundary(beginTime.plus(TimeFrameMapping.getDuration(TimeFrame.MINUTES_1)), timeFrame)
        return thisBucket != nextBucket
    }

    /**
     * Creates an initial aggregated bar from a single bar.
     */
    private fun createInitialAggregatedBar(bar: Bar, timeframe: TimeFrame, beginTime: Instant): AggregatedBar {
        val period = TimeFrameMapping.getDuration(timeframe)
        val endTime = beginTime.plus(period)

        return AggregatedBar(
            timeFrame = timeframe,
            timePeriod = period,
            beginTime = beginTime,
            endTime = endTime,
            openPrice = bar.openPrice,
            highPrice = bar.highPrice,
            lowPrice = bar.lowPrice,
            closePrice = bar.closePrice,
            volume = bar.volume
        )
    }

    /**
     * Updates an existing aggregated bar with a new bar.
     */
    private fun updateAggregatedBar(aggregatedBar: AggregatedBar, newBar: Bar): AggregatedBar {
        return aggregatedBar.copy(
            highPrice = maxOf(aggregatedBar.highPrice, newBar.highPrice),
            lowPrice = minOf(aggregatedBar.lowPrice, newBar.lowPrice),
            closePrice = newBar.closePrice,
            volume = aggregatedBar.volume + newBar.volume
        )
    }

    /**
     * Notifies all listeners of a new aggregated bar.
     */
    private fun notifyListeners(bar: Bar) {
        listeners.forEach { listener ->
            listener.onBar(bar)
        }
    }
}
