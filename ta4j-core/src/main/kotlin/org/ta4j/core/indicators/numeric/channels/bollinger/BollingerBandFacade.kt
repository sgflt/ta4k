/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * A facade to create the 3 Bollinger Band indicators. A simple moving average
 * of close price is used as the middle band. The BB bandwidth and %B indicators
 * can also be created on demand.
 *
 *
 *
 * This class creates lightweight "fluent" numeric indicators. These objects are
 * not cached, although they may be wrapped around cached objects. Overall there
 * is less caching and probably better performance.
 */
class BollingerBandFacade(
    price: NumericIndicator,
    barCount: Int,
    k: Number,
) {
    val middle: NumericIndicator = price.sma(barCount)
    val upper: NumericIndicator = middle.plus(price.stddev(barCount).multipliedBy(k))
    val lower: NumericIndicator = middle.minus(price.stddev(barCount).multipliedBy(k))
    val bandwidth: NumericIndicator = upper.minus(lower).dividedBy(middle).multipliedBy(100)
    val percentB: NumericIndicator = price.minus(lower).dividedBy(upper.minus(lower))
}
