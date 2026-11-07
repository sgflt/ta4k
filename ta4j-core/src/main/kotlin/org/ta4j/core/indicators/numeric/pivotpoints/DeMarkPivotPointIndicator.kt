/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.pivotpoints

import java.time.ZoneOffset
import java.time.temporal.IsoFields
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * DeMark Pivot Point indicator.
 *
 * The DeMark Pivot Point uses a different calculation than traditional pivot points.
 * It takes into account the relationship between the open and close prices:
 *
 * - If Close < Open: X = High + (2 × Low) + Close
 * - If Close > Open: X = (2 × High) + Low + Close
 * - If Close = Open: X = High + Low + (2 × Close)
 * - Pivot Point = X ÷ 4
 *
 * @param numFactory the number factory for calculations
 * @param timeLevel the time level for pivot calculation
 *
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:pivot_points">
 *      StockCharts - Pivot Points</a>
 */
class DeMarkPivotPointIndicator(
    numFactory: NumFactory,
    private val timeLevel: TimeLevel,
) : NumericIndicator(numFactory) {

    private val two = numFactory.two()
    private val four = numFactory.numOf(4)

    // Previous period data for pivot calculation
    private var previousPeriodHigh: Num = NaN
    private var previousPeriodLow: Num = NaN
    private var previousPeriodOpen: Num = NaN
    private var previousPeriodClose: Num = NaN

    // Current period tracking
    private var currentPeriodId: Long? = null
    private var currentPeriodHigh: Num = NaN
    private var currentPeriodLow: Num = NaN
    private var currentPeriodOpen: Num = NaN
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
                // First bar ever
                initializeFirstPeriod(bar, barPeriodId)
            }

            barPeriodId != currentPeriodId -> {
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
            value = calculatePivot(previousPeriodHigh, previousPeriodLow, previousPeriodOpen, previousPeriodClose)
        }

        // Store current bar data for next calculation
        previousPeriodHigh = bar.highPrice
        previousPeriodLow = bar.lowPrice
        previousPeriodOpen = bar.openPrice
        previousPeriodClose = bar.closePrice
        isFirstPeriod = false
    }

    private fun initializeFirstPeriod(bar: Bar, periodId: Long) {
        currentPeriodId = periodId
        currentPeriodHigh = bar.highPrice
        currentPeriodLow = bar.lowPrice
        currentPeriodOpen = bar.openPrice
        lastCloseInPeriod = bar.closePrice
    }

    private fun finalizePreviousPeriod() {
        previousPeriodHigh = currentPeriodHigh
        previousPeriodLow = currentPeriodLow
        previousPeriodOpen = currentPeriodOpen
        previousPeriodClose = lastCloseInPeriod
    }

    private fun calculatePivotPoint() {
        if (!isFirstPeriod && !previousPeriodHigh.isNaN) {
            value = calculatePivot(previousPeriodHigh, previousPeriodLow, previousPeriodOpen, previousPeriodClose)
        }
        isFirstPeriod = false
    }

    private fun startNewPeriod(bar: Bar, periodId: Long) {
        currentPeriodId = periodId
        currentPeriodHigh = bar.highPrice
        currentPeriodLow = bar.lowPrice
        currentPeriodOpen = bar.openPrice
    }

    private fun updateCurrentPeriodData(bar: Bar) {
        currentPeriodHigh = maxOf(currentPeriodHigh, bar.highPrice)
        currentPeriodLow = minOf(currentPeriodLow, bar.lowPrice)
    }

    private fun calculatePivot(high: Num, low: Num, open: Num, close: Num): Num {
        val x = when {
            close < open -> high + two * low + close
            close > open -> two * high + low + close
            else -> high + low + two * close
        }
        return x / four
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

    override val lag = when (timeLevel) {
        TimeLevel.BARBASED -> 1
        else -> 0 // Will be stable after the first complete period
    }

    override val isStable: Boolean
        get() = !isFirstPeriod && !value.isNaN


    override fun toString(): String {
        return "DeMarkPivotPoint($timeLevel) => $value"
    }
}
