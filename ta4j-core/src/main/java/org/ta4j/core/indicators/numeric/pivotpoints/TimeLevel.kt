package org.ta4j.core.indicators.numeric.pivotpoints

/**
 * Time levels for pivot point calculation.
 */
enum class TimeLevel {
    /**
     * Use just the last bar data for calculation.
     */
    BARBASED,

    /**
     * Use prior day's high, low, open and close for calculation.
     * Suitable for 1-, 5-, 10- and 15-minute charts.
     */
    DAY,

    /**
     * Use prior week's high, low, open and close for calculation.
     * Suitable for 30-, 60- and 120-minute charts.
     */
    WEEK,

    /**
     * Use prior month's high, low, open and close for calculation.
     * Suitable for daily charts.
     */
    MONTH,

    /**
     * Use prior year's high, low, open and close for calculation.
     * Suitable for weekly and monthly charts.
     */
    YEAR
}
