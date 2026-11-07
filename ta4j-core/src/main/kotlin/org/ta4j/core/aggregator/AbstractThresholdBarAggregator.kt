/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.aggregator

import java.time.Duration
import java.time.Instant
import org.ta4j.core.api.callback.TickListener
import org.ta4j.core.api.series.Bar
import org.ta4j.core.events.TickReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Builds a bar once the summed [contribution] of source bars reaches [threshold].
 *
 * With [carryOverRemainder], a source bar that overshoots closes the bar exactly at the threshold
 * and the overshoot (with its proportional volume) starts the next bar at that source bar's close.
 * Ticks are treated as single-price bars with zero volume.
 */
abstract class AbstractThresholdBarAggregator(
    protected val numFactory: NumFactory,
    threshold: Number,
    private val timeFrame: TimeFrame,
    private val carryOverRemainder: Boolean,
) : AbstractBarTransformer(), TickListener {

    protected val threshold: Num = numFactory.numOf(threshold)

    init {
        require(this.threshold.isPositive) { "Threshold must be positive, but was: $threshold" }
    }

    private var current: AggregatedBar? = null
    private var progress: Num = numFactory.zero()

    /** How far [bar] moves the current bar toward [threshold]. */
    protected abstract fun contribution(bar: Bar): Num

    override fun onBar(bar: Bar) {
        val contribution = contribution(bar)
        current = current?.merge(bar) ?: bar.toAggregated()
        progress += contribution

        while (progress >= threshold) {
            val remainder = progress - threshold
            val closing = current!!
            if (!carryOverRemainder || remainder.isZero || contribution.isZero) {
                emit(closing)
                current = null
                progress = numFactory.zero()
                return
            }
            val carriedVolume = bar.volume * (remainder / contribution)
            emit(closing.copy(volume = closing.volume - carriedVolume))
            current = AggregatedBar(
                timeFrame = timeFrame,
                timePeriod = Duration.ZERO,
                beginTime = bar.endTime,
                endTime = bar.endTime,
                openPrice = bar.closePrice,
                highPrice = bar.closePrice,
                lowPrice = bar.closePrice,
                closePrice = bar.closePrice,
                volume = carriedVolume,
            )
            progress = remainder
        }
    }

    override fun onTick(tick: TickReceived) {
        val price = numFactory.numOf((tick.ask + tick.bid) / 2.0)
        onBar(pointBar(tick.beginTime, price))
    }

    private fun pointBar(time: Instant, price: Num) = AggregatedBar(
        timeFrame = timeFrame,
        timePeriod = Duration.ZERO,
        beginTime = time,
        endTime = time,
        openPrice = price,
        highPrice = price,
        lowPrice = price,
        closePrice = price,
        volume = numFactory.zero(),
    )

    private fun Bar.toAggregated() = AggregatedBar(
        timeFrame = this@AbstractThresholdBarAggregator.timeFrame,
        timePeriod = Duration.between(beginTime, endTime),
        beginTime = beginTime,
        endTime = endTime,
        openPrice = openPrice,
        highPrice = highPrice,
        lowPrice = lowPrice,
        closePrice = closePrice,
        volume = volume,
    )

    private fun AggregatedBar.merge(bar: Bar) = copy(
        timePeriod = Duration.between(beginTime, bar.endTime),
        endTime = bar.endTime,
        highPrice = if (bar.highPrice > highPrice) bar.highPrice else highPrice,
        lowPrice = if (bar.lowPrice < lowPrice) bar.lowPrice else lowPrice,
        closePrice = bar.closePrice,
        volume = volume + bar.volume,
    )
}

/** A bar every [count] source bars or ticks. */
class TickBarAggregator(numFactory: NumFactory, count: Int) :
    AbstractThresholdBarAggregator(numFactory, count, TimeFrame("tick-$count"), carryOverRemainder = false) {
    override fun contribution(bar: Bar): Num = numFactory.one()
}

/** A bar every [volume] traded units; overshoot carries into the next bar. */
class VolumeBarAggregator(numFactory: NumFactory, volume: Number) :
    AbstractThresholdBarAggregator(numFactory, volume, TimeFrame("volume-$volume"), carryOverRemainder = true) {
    override fun contribution(bar: Bar): Num = bar.volume
}

/** A bar every [amount] of traded value (close * volume); overshoot carries into the next bar. */
class AmountBarAggregator(numFactory: NumFactory, amount: Number) :
    AbstractThresholdBarAggregator(numFactory, amount, TimeFrame("amount-$amount"), carryOverRemainder = true) {
    override fun contribution(bar: Bar): Num = bar.closePrice * bar.volume
}
