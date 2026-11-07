/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Modified moving average indicator.
 *
 *
 *
 * It is similar to exponential moving average but smooths more slowly. Used in
 * Welles Wilder's indicators like ADX, RSI.
 */
class MMAIndicator(indicator: NumericIndicator, barCount: Int) :
    AbstractEMAIndicator(indicator, barCount, 1.0 / barCount) {
    override fun toString() = "MMA($barCount) => $value"
}
