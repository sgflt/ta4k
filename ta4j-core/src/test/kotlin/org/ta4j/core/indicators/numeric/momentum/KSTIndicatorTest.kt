/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.momentum

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class KSTIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `should calculate KST indicator values correctly`(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(
                1344.78, 1357.98, 1355.69, 1325.51, 1335.02, 1313.72, 1319.99, 1331.85, 1329.04, 1362.16,
                1365.51, 1374.02, 1367.58, 1354.68, 1352.46, 1341.47, 1341.45, 1334.76, 1356.78, 1353.64,
                1363.67, 1372.78, 1376.51, 1362.66, 1350.52, 1338.31, 1337.89, 1360.02, 1385.97, 1385.30,
                1379.32, 1375.32, 1365.00, 1390.99, 1394.23, 1401.35, 1402.22, 1402.80, 1405.87, 1404.11,
                1403.93, 1405.53, 1415.51, 1418.16, 1418.13, 1413.17, 1413.49, 1402.08, 1411.13, 1410.44
            )

        val closePrice = Indicators.extended(numFactory).closePrice()
        val kst = KSTIndicator(numFactory, closePrice)

        context.withIndicator(kst)

        // Fast forward to index 44 (45th bar)
        context.fastForwardUntilStable()
        context.assertCurrent(36.597637)

        // Index 45 (46th bar)
        context.assertNext(37.228478)

        // Index 46 (47th bar)
        context.assertNext(38.381911)

        // Index 47 (48th bar)
        context.assertNext(38.783888)

        // Index 48 (49th bar)
        context.assertNext(37.543147)

        // Index 49 (50th bar)
        context.assertNext(36.253502)
    }

    @Test
    fun `should validate constructor parameters`() {
        val numFactory = org.ta4j.core.num.DoubleNumFactory
        val closePrice = Indicators.extended(numFactory).closePrice()

        assertThrows<IllegalArgumentException> {
            KSTIndicator(numFactory, closePrice, rcma1SMABarCount = 0)
        }

        assertThrows<IllegalArgumentException> {
            KSTIndicator(numFactory, closePrice, rcma1ROCBarCount = -1)
        }

        assertThrows<IllegalArgumentException> {
            KSTIndicator(numFactory, closePrice, rcma2SMABarCount = 0)
        }

        assertThrows<IllegalArgumentException> {
            KSTIndicator(numFactory, closePrice, rcma4ROCBarCount = -5)
        }
    }
}
