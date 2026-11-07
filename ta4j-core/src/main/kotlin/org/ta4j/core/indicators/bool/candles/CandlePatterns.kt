/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.candles

import org.ta4j.core.indicators.bool.candles.CandlePatternIndicator.Companion.DEFAULT_AVERAGE_PERIOD

/**
 * Entry point for candlestick patterns, e.g. `Indicators.candlePatterns.hammer().toRule()`.
 *
 * [averagePeriod] is the number of preceding candles whose average body and range define
 * "long", "short" and "near".
 */
object CandlePatterns {
    fun hammer(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) = HammerIndicator(averagePeriod)
    fun hangingMan(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) = HangingManIndicator(averagePeriod)
    fun invertedHammer(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) = InvertedHammerIndicator(averagePeriod)
    fun shootingStar(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) = ShootingStarIndicator(averagePeriod)
    fun bullishMarubozu(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) = BullishMarubozuIndicator(averagePeriod)
    fun bearishMarubozu(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) = BearishMarubozuIndicator(averagePeriod)
    fun bullishKicker(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) = BullishKickerIndicator(averagePeriod)
    fun bearishKicker(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) = BearishKickerIndicator(averagePeriod)
    fun threeInsideUp(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) = ThreeInsideUpIndicator(averagePeriod)
    fun threeInsideDown(averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) = ThreeInsideDownIndicator(averagePeriod)

    fun darkCloudCover(penetration: Double = 0.5, averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) =
        DarkCloudCoverIndicator(penetration, averagePeriod)

    fun piercingLine(penetration: Double = 0.5, averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) =
        PiercingLineIndicator(penetration, averagePeriod)

    fun morningStar(penetration: Double = 0.5, averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) =
        MorningStarIndicator(penetration, averagePeriod)

    fun eveningStar(penetration: Double = 0.5, averagePeriod: Int = DEFAULT_AVERAGE_PERIOD) =
        EveningStarIndicator(penetration, averagePeriod)
}
