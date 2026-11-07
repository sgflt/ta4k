/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators

import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.indicators.bool.BooleanIndicator

/**
 * Indicator that fetches data from series directly
 */
abstract class SeriesRelatedBooleanIndicator protected constructor(val barSeries: BarSeries) : BooleanIndicator()
