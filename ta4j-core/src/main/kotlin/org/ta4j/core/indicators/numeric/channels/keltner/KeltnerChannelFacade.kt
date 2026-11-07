/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.keltner

import org.ta4j.core.api.Indicators.atr
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.EMAIndicator

/**
 * A facade to create the 3 Keltner Channel indicators. An exponential moving
 * average of close price is used as the middle channel.
 *
 *
 *
 * This class creates lightweight "fluent" numeric indicators. These objects are
 * not cached, although they may be wrapped around cached objects. Overall there
 * is less caching and probably better performance.
 */
class KeltnerChannelFacade(emaCount: Int, atrCount: Int, k: Number) {
    private val middle: EMAIndicator
    private val upper: NumericIndicator
    private val lower: NumericIndicator


    /**
     * Constructor.
     *
     * @param emaCount the bar count for the `EmaIndicator`
     * @param atrCount the bar count for the `ATRIndicator`
     * @param k the multiplier for the [.upper] and [.lower]
     * channel
     */
    init {
        val price = closePrice()
        val atr = atr(atrCount)
        this.middle = price.ema(emaCount)
        this.upper = this.middle.plus(atr.multipliedBy(k))
        this.lower = this.middle.minus(atr.multipliedBy(k))
    }


    /** @return the middle channel
     */
    fun middle(): EMAIndicator {
        return this.middle
    }


    /** @return the upper channel
     */
    fun upper(): NumericIndicator {
        return this.upper
    }


    /** @return the lower channel
     */
    fun lower(): NumericIndicator {
        return this.lower
    }
}
