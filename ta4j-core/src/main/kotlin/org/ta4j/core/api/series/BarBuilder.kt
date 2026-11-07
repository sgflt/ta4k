/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.api.series

import java.time.Instant

/**
 * @author Lukáš Kvídera
 */
interface BarBuilder {
    fun startTime(startTime: Instant): BarBuilder

    fun endTime(endTime: Instant): BarBuilder

    fun openPrice(openPrice: Number): BarBuilder

    fun closePrice(closePrice: Number): BarBuilder

    fun highPrice(highPrice: Number): BarBuilder

    fun lowPrice(lowPrice: Number): BarBuilder

    fun volume(volume: Number): BarBuilder

    /**
     * Build this bar and onCandle him to related [BarSeries]
     */
    fun add()
}
