/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.trading

import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries

/**
 * @author Lukáš Kvídera
 */
internal class LightweightBarBuilderFactory : BarBuilderFactory {
    override fun createBarBuilder(series: BarSeries) = LightweightBarBuilder(series)
}
