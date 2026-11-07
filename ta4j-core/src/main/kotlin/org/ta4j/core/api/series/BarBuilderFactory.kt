/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.api.series

/**
 * A factory that provides builders of bars.
 */
fun interface BarBuilderFactory {
    fun createBarBuilder(series: BarSeries): BarBuilder
}
