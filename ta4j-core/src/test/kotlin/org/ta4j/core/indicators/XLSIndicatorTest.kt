/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators

import org.ta4j.core.ExternalIndicatorTest
import org.ta4j.core.XlsTestsUtils
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.mocks.MockIndicator
import org.ta4j.core.num.NumFactory

class XLSIndicatorTest(
    private val clazz: Class<*>,
    private val fileName: String,
    private val column: Int,
    private val numFactory: NumFactory,
) : ExternalIndicatorTest {

    override val marketEvents: List<MarketEvent> by lazy { XlsTestsUtils.getMarketEvents(clazz, fileName) }

    override fun getIndicator(vararg params: Any): MockIndicator {
        return XlsTestsUtils.getIndicator(
            clazz,
            fileName,
            column,
            numFactory,
            *params
        )
    }
}
