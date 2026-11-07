/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.mocks

import org.ta4j.core.api.series.BarBuilder
import org.ta4j.core.api.series.BarBuilderFactory
import org.ta4j.core.api.series.BarSeries

class MockBarBuilderFactory : BarBuilderFactory {

    override fun createBarBuilder(series: BarSeries): BarBuilder {
        return MockBarBuilder(series)
    }
}