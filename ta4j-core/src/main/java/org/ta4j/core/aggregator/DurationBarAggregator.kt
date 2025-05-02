/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
package org.ta4j.core.aggregator

import org.ta4j.core.api.series.Bar
import org.ta4j.core.backtest.BacktestBar
import org.ta4j.core.num.Num
import java.time.Duration
import java.time.Instant

/**
 * Aggregates a list of [bars][BacktestBar] into another one by
 * [duration][BacktestBar.timePeriod].
 */
class DurationBarAggregator
/**
 * Duration based bar aggregator. Only bars with elapsed time (final bars) will
 * be created.
 *
 * @param timePeriod the target time period that aggregated bars should have
 */ @JvmOverloads constructor(
    /** The target time period that aggregated bars should have.  */
    private val timePeriod: Duration,
    private val onlyFinalBars: Boolean = true,
) : BarAggregator {
    /**
     * Duration based bar aggregator.
     *
     * @param timePeriod    the target time period that aggregated bars should have
     * @param onlyFinalBars if true, only bars with elapsed time (final bars) will
     * be created, otherwise also pending bars
     */

    /**
     * Aggregates the `bars` into another one by [.timePeriod].
     *
     * @param bars the actual bars with actual `timePeriod`
     * @return the aggregated bars with new [.timePeriod]
     * @throws IllegalArgumentException if [.timePeriod] is not a
     * multiplication of actual `timePeriod`
     */
    override fun aggregate(bars: List<Bar>): MutableList<Bar> {
        val aggregated: MutableList<Bar> = ArrayList<Bar>()
        if (bars.isEmpty()) {
            return aggregated
        }
        val firstBar = bars[0]
        // get the actual time period
        val actualDur = firstBar.timePeriod
        // check if new timePeriod is a multiplication of actual time period
        val isMultiplication = this.timePeriod.seconds % actualDur.seconds == 0L
        require(isMultiplication) { "Cannot aggregate bars: the new timePeriod must be a multiplication of the actual timePeriod." }

        var i = 0
        val zero = firstBar.openPrice.numFactory.zero()
        while (i < bars.size) {
            var bar = bars[i] as BacktestBar
            val beginTime = bar.beginTime
            bar.openPrice
            var high = bar.highPrice
            var low = bar.lowPrice

            var close: Num? = null
            var volume = zero
            var amount = zero
            var trades: Long = 0
            var sumDur = Duration.ZERO

            while (isInDuration(sumDur)) {
                if (i < bars.size) {
                    if (!beginTimesInDuration(beginTime, bars[i].beginTime)) {
                        break
                    }
                    bar = bars[i] as BacktestBar
                    if (bar.highPrice.isGreaterThan(high)) {
                        high = bar.highPrice
                    }
                    if (bar.lowPrice.isLessThan(low)) {
                        low = bar.lowPrice
                    }
                    close = bar.closePrice

                    volume = volume.plus(bar.volume)
                    amount = amount.plus(bar.amount)
                    if (bar.trades != 0L) {
                        trades = trades + bar.trades
                    }
                }

                sumDur = sumDur.plus(actualDur)
                i++
            }

            if (!this.onlyFinalBars || i <= bars.size) {
// FIXME how to aggregate bars without accessmto series?               final Bar aggregatedBar = new BacktestBarBuilder(new MockBarSeriesBuilder().build()).timePeriod(this.timePeriod)
//                        .endTime(beginTime.plus(this.timePeriod))
//                        .openPrice(open)
//                        .highPrice(high)
//                        .lowPrice(low)
//                        .closePrice(close)
//                        .volume(volume)
//                        .amount(amount)
//                        .trades(trades)
//                        .build();
//                aggregated.onCandle(aggregatedBar);
            }
        }

        return aggregated
    }

    private fun beginTimesInDuration(startTime: Instant, endTime: Instant?): Boolean {
        return Duration.between(startTime, endTime).compareTo(this.timePeriod) < 0
    }

    private fun isInDuration(duration: Duration): Boolean {
        return duration.compareTo(this.timePeriod) < 0
    }
}
