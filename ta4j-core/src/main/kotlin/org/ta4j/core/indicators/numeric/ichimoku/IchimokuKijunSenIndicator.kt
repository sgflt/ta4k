/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.ichimoku

import org.ta4j.core.num.NumFactory

/**
 * Ichimoku clouds: Kijun-sen (Base line) indicator.
 *
 * @see [Ichimoku Cloud](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud)
 */
class IchimokuKijunSenIndicator(
    numFactory: NumFactory,
    barCount: Int = 26,
) : IchimokuLineIndicator(numFactory, barCount)
