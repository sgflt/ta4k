/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
    private val numFactory: NumFactory
) : ExternalIndicatorTest {

    private var cachedSeries: List<MarketEvent>? = null

    override fun getMarketEvents(): List<MarketEvent> {
        if (cachedSeries == null) {
            cachedSeries = XlsTestsUtils.getMarketEvents(clazz, fileName)
        }
        return cachedSeries!!
    }

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