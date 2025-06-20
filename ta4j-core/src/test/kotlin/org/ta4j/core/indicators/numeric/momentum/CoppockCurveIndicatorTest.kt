/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators
import org.ta4j.core.num.NumFactory

class CoppockCurveIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun coppockCurveWithRoc14Roc11Wma10(numFactory: NumFactory) {
        // Example from
        // http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:coppock_curve
        val prices = doubleArrayOf(
            872.81, 919.14, 919.32, 987.48, 1020.62, 1057.08, 1036.19, 1095.63, 1115.1, 1073.87,
            1104.49, 1169.43, 1186.69, 1089.41, 1030.71, 1101.6, 1049.33, 1141.2, 1183.26, 1180.55,
            1257.64, 1286.12, 1327.22, 1325.83, 1363.61, 1345.2, 1320.64, 1292.28, 1218.89, 1131.42,
            1253.3, 1246.96, 1257.6, 1312.41, 1365.68, 1408.47, 1397.91, 1310.33, 1362.16, 1379.32
        )

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(*prices)

        val closePrice = Indicators.extended(numFactory).closePrice()
        val coppock = closePrice.coppock(14, 11, 10)

        context.withIndicator(coppock)

        context.fastForward(31)

        context.assertNext(23.8929)
        context.assertNext(19.3187)
        context.assertNext(16.3505)
        context.assertNext(14.12)
        context.assertNext(12.782)
        context.assertNext(11.3924)
        context.assertNext(8.3662)
        context.assertNext(7.4532)
        context.assertNext(8.79)
    }
}
