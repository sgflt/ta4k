/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.donchian

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Donchian Channel Facade.
 *
 * This facade provides convenient access to all three Donchian Channel lines:
 * - Upper Channel: Highest high over the specified period
 * - Lower Channel: Lowest low over the specified period
 * - Middle Channel: Average of upper and lower channels
 *
 * Donchian Channels are used to identify potential breakouts and support/resistance levels.
 * They are particularly useful for trend-following strategies and volatility analysis.
 *
 * @param barCount the time frame (number of periods)
 * @param numFactory the number factory for calculations
 *
 * @see <a href="https://www.investopedia.com/terms/d/donchianchannels.asp">
 *      Investopedia - Donchian Channels</a>
 * @see <a href="https://www.fidelity.com/learning-center/trading-investing/technical-analysis/technical-indicator-guide/donchian-channel">
 *      Fidelity - Donchian Channel Guide</a>
 */
class DonchianChannelFacade(
    private val barCount: Int,
    private val numFactory: NumFactory,
) {
    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    /**
     * The upper Donchian Channel line (highest high over the period).
     */
    val upper: NumericIndicator by lazy {
        DonchianChannelUpperIndicator(numFactory, barCount)
    }

    /**
     * The lower Donchian Channel line (lowest low over the period).
     */
    val lower: NumericIndicator by lazy {
        DonchianChannelLowerIndicator(numFactory, barCount)
    }

    /**
     * The middle Donchian Channel line (average of upper and lower lines).
     */
    val middle: DonchianChannelMiddleIndicator by lazy {
        DonchianChannelMiddleIndicator(numFactory, barCount)
    }
}
