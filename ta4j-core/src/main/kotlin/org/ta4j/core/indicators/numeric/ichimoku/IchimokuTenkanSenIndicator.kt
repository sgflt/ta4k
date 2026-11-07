/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.ichimoku

import org.ta4j.core.num.NumFactory

/**
 * Ichimoku clouds: Tenkan-sen (Conversion line) indicator.
 *
 * @see [Ichimoku Cloud](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud)
 */
class IchimokuTenkanSenIndicator(
    numFactory: NumFactory,
    barCount: Int = 9,
) : IchimokuLineIndicator(numFactory, barCount)
