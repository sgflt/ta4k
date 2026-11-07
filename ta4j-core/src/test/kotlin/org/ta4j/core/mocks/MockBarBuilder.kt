/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.mocks

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.backtest.BacktestBar
import org.ta4j.core.backtest.BacktestBarBuilder

class MockBarBuilder(series: BarSeries) : BacktestBarBuilder(series) {

    private val clock = Clock.fixed(Instant.ofEpochMilli(-1), ZoneId.systemDefault())
    private var periodSet = false
    private var endTimeSet = false
    private var timePeriod: Duration = Duration.ofDays(1)

    override fun endTime(endTime: Instant): MockBarBuilder {
        endTimeSet = true
        super.endTime(endTime)
        return this
    }

    fun timePeriod(timePeriod: Duration): MockBarBuilder {
        periodSet = true
        this.timePeriod = timePeriod
        return this
    }

    override fun build(): BacktestBar {
        if (!periodSet) {
            timePeriod(Duration.ofDays(1))
        }

        if (!endTimeSet) {
            endTime(Instant.now(Clock.offset(clock, timePeriod.multipliedBy(++countOfProducedBars))))
        }
        return super.build()
    }

    companion object {
        private var countOfProducedBars: Long = 0
    }
}
