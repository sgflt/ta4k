/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Exponential moving average indicator.
 *
 * @see [https://www.investopedia.com/terms/e/ema.asp](https://www.investopedia.com/terms/e/ema.asp)
 */
class EMAIndicator(indicator: NumericIndicator, barCount: Int) :
    AbstractEMAIndicator(indicator, barCount, (2.0 / (barCount + 1))) {
    override fun toString() = "EMA(${barCount}) => $value"
}
