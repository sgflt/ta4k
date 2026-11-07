/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Base for MACD-style spreads; adds the signal line and histogram.
 */
abstract class AbstractMACDIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {

    /** EMA of this spread. */
    fun signalLine(barCount: Int = 9) = ema(barCount)

    /** Spread minus its signal line. */
    fun histogram(signalBarCount: Int = 9) = minus(signalLine(signalBarCount))
}
