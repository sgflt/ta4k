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
package org.ta4j.core.indicators.numeric.pivotpoints

import java.time.ZoneOffset
import java.time.temporal.IsoFields
import org.slf4j.LoggerFactory
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Pivot Point indicator.
 *
 * Calculates pivot points based on the previous period's high, low, and close prices.
 * The pivot point is calculated as: (High + Low + Close) / 3
 *
 * Time level usage:
 * - 1-, 5-, 10- and 15-minute charts use the prior day's high, low and close: TimeLevel.DAY
 * - 30-, 60- and 120-minute charts use the prior week's high, low, and close: TimeLevel.WEEK
 * - Daily charts use the prior month's high, low and close: TimeLevel.MONTH
 * - Weekly and monthly charts use the prior year's high, low and close: TimeLevel.YEAR
 * - For bar-based calculation using just the previous bar: TimeLevel.BARBASED
 *
 * @param numFactory the number factory for calculations
 * @param timeLevel the time level for pivot calculation
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:pivot_points">
 *      StockCharts - Pivot Points</a>
 */
class PivotPointIndicator(
    numFactory: NumFactory,
    private val timeLevel: TimeLevel,
) : NumericIndicator(numFactory) {

    private val three = numFactory.three()

    // Previous period data for pivot calculation
    private var previousPeriodHigh: Num = NaN
    private var previousPeriodLow: Num = NaN
    private var previousPeriodClose: Num = NaN

    // Current period tracking
    private var currentPeriodId: Long? = null
    private var currentPeriodHigh: Num = NaN
    private var currentPeriodLow: Num = NaN
    private var lastCloseInPeriod: Num = NaN
    private var isFirstPeriod = true

    override fun updateState(bar: Bar) {
        if (timeLevel == TimeLevel.BARBASED) {
            handleBarBasedCalculation(bar)
            return
        }

        val barPeriodId = getPeriodId(bar)

        when {
            currentPeriodId == null -> {
                log.atTrace().log { "Starting the first period on bar (${bar.beginTime}, ${bar.endTime})" }
                // First bar ever
                initializeFirstPeriod(bar, barPeriodId)
            }

            barPeriodId != currentPeriodId -> {
                log.atTrace().log { "Starting a new period on bar (${bar.beginTime}, ${bar.endTime})" }
                // New period started
                finalizePreviousPeriod()
                calculatePivotPoint()
                startNewPeriod(bar, barPeriodId)
            }

            else -> {
                // Same period - update current period data
                updateCurrentPeriodData(bar)
            }
        }

        // Always update the last close in current period
        lastCloseInPeriod = bar.closePrice
    }

    private fun handleBarBasedCalculation(bar: Bar) {
        // For bar-based, use previous bar's data to calculate pivot
        if (!isFirstPeriod) {
            value = calculatePivot(previousPeriodHigh, previousPeriodLow, previousPeriodClose)
        }

        // Store current bar data for next calculation
        previousPeriodHigh = bar.highPrice
        previousPeriodLow = bar.lowPrice
        previousPeriodClose = bar.closePrice
        isFirstPeriod = false
    }

    private fun initializeFirstPeriod(bar: Bar, periodId: Long) {
        currentPeriodId = periodId
        currentPeriodHigh = bar.highPrice
        currentPeriodLow = bar.lowPrice
        lastCloseInPeriod = bar.closePrice
    }

    private fun finalizePreviousPeriod() {
        previousPeriodHigh = currentPeriodHigh
        previousPeriodLow = currentPeriodLow
        previousPeriodClose = lastCloseInPeriod
    }

    private fun calculatePivotPoint() {
        if (!isFirstPeriod && !previousPeriodHigh.isNaN) {
            value = calculatePivot(previousPeriodHigh, previousPeriodLow, previousPeriodClose)
        }
        isFirstPeriod = false
    }

    private fun startNewPeriod(bar: Bar, periodId: Long) {
        currentPeriodId = periodId
        currentPeriodHigh = bar.highPrice
        currentPeriodLow = bar.lowPrice
    }

    private fun updateCurrentPeriodData(bar: Bar) {
        currentPeriodHigh = maxOf(currentPeriodHigh, bar.highPrice)
        currentPeriodLow = minOf(currentPeriodLow, bar.lowPrice)
    }

    private fun calculatePivot(high: Num, low: Num, close: Num): Num {
        return (high + low + close) / three
    }

    private fun getPeriodId(bar: Bar): Long {
        val zonedDateTime = bar.endTime.atZone(ZoneOffset.UTC)
        return when (timeLevel) {
            TimeLevel.DAY -> zonedDateTime.dayOfYear.toLong()
            TimeLevel.WEEK -> zonedDateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toLong()
            TimeLevel.MONTH -> zonedDateTime.monthValue.toLong()
            TimeLevel.YEAR -> zonedDateTime.year.toLong()
            TimeLevel.BARBASED -> 0L // Not used for bar-based
        }
    }

    override val lag = 3

    override val isStable: Boolean
        get() = !isFirstPeriod && !value.isNaN

    override fun toString(): String {
        return "PivotPoint($timeLevel) => $value"
    }

    companion object {
        private val log = LoggerFactory.getLogger(PivotPointIndicator::class.java)
    }
}
