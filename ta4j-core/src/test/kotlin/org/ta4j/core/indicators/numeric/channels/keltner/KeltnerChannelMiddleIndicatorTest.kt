/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.channels.keltner

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.mocks.MockMarketEventBuilder
import org.ta4j.core.num.NumFactory

class KeltnerChannelMiddleIndicatorTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun keltnerChannelMiddleIndicatorTest(numFactory: NumFactory) {
        val marketEvents = MockMarketEventBuilder()
            .candle().openPrice(11577.43).closePrice(11670.75).highPrice(11711.47).lowPrice(11577.35).add()
            .candle().openPrice(11670.90).closePrice(11691.18).highPrice(11698.22).lowPrice(11635.74).add()
            .candle().openPrice(11688.61).closePrice(11722.89).highPrice(11742.68).lowPrice(11652.89).add()
            .candle().openPrice(11716.93).closePrice(11697.31).highPrice(11736.74).lowPrice(11667.46).add()
            .candle().openPrice(11696.86).closePrice(11674.76).highPrice(11726.94).lowPrice(11599.68).add()
            .candle().openPrice(11672.34).closePrice(11637.45).highPrice(11677.33).lowPrice(11573.87).add()
            .candle().openPrice(11638.51).closePrice(11671.88).highPrice(11704.12).lowPrice(11635.48).add()
            .candle().openPrice(11673.62).closePrice(11755.44).highPrice(11782.23).lowPrice(11673.62).add()
            .candle().openPrice(11753.70).closePrice(11731.90).highPrice(11757.25).lowPrice(11700.53).add()
            .candle().openPrice(11732.13).closePrice(11787.38).highPrice(11794.15).lowPrice(11698.83).add()
            .candle().openPrice(11783.82).closePrice(11837.93).highPrice(11858.78).lowPrice(11777.99).add()
            .candle().openPrice(11834.21).closePrice(11825.29).highPrice(11861.24).lowPrice(11798.46).add()
            .candle().openPrice(11823.70).closePrice(11822.80).highPrice(11845.16).lowPrice(11744.77).add()
            .candle().openPrice(11822.95).closePrice(11871.84).highPrice(11905.48).lowPrice(11822.80).add()
            .candle().openPrice(11873.43).closePrice(11980.52).highPrice(11982.94).lowPrice(11867.98).add()
            .candle().openPrice(11980.52).closePrice(11977.19).highPrice(11985.97).lowPrice(11898.74).add()
            .candle().openPrice(11978.85).closePrice(11985.44).highPrice(12020.52).lowPrice(11961.83).add()
            .candle().openPrice(11985.36).closePrice(11989.83).highPrice(12019.53).lowPrice(11971.93).add()
            .candle().openPrice(11824.39).closePrice(11891.93).highPrice(11891.93).lowPrice(11817.88).add()
            .candle().openPrice(11892.50).closePrice(12040.16).highPrice(12050.75).lowPrice(11892.50).add()
            .candle().openPrice(12038.27).closePrice(12041.97).highPrice(12057.91).lowPrice(12018.51).add()
            .candle().openPrice(12040.68).closePrice(12062.26).highPrice(12080.54).lowPrice(11981.05).add()
            .candle().openPrice(12061.73).closePrice(12092.15).highPrice(12092.42).lowPrice(12025.78).add()
            .candle().openPrice(12092.38).closePrice(12161.63).highPrice(12188.76).lowPrice(12092.30).add()
            .candle().openPrice(12152.70).closePrice(12233.15).highPrice(12238.79).lowPrice(12150.05).add()
            .candle().openPrice(12229.29).closePrice(12239.89).highPrice(12254.23).lowPrice(12188.19).add()
            .candle().openPrice(12239.66).closePrice(12229.29).highPrice(12239.66).lowPrice(12156.94).add()
            .candle().openPrice(12227.78).closePrice(12273.26).highPrice(12285.94).lowPrice(12180.48).add()
            .candle().openPrice(12266.83).closePrice(12268.19).highPrice(12276.21).lowPrice(12235.91).add()
            .candle().openPrice(12266.75).closePrice(12226.64).highPrice(12267.66).lowPrice(12193.27).add()
            .candle().openPrice(12219.79).closePrice(12288.17).highPrice(12303.16).lowPrice(12219.79).add()
            .candle().openPrice(12287.72).closePrice(12318.14).highPrice(12331.31).lowPrice(12253.24).add()
            .candle().openPrice(12389.74).closePrice(12212.79).highPrice(12389.82).lowPrice(12176.31).add()
            .build()

        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(marketEvents)

        val closePriceIndicator = closePrice()
        val km = KeltnerChannelMiddleIndicator(numFactory, closePriceIndicator, 14)

        context.withIndicator(km)
            .fastForwardUntilStable()
            .assertCurrent(11764.23)
            .assertNext(11793.0687)
            .assertNext(11817.6182)
            .assertNext(11839.9944)
            .assertNext(11859.9725)
            .assertNext(11864.2335)
            .assertNext(11887.6903)
            .assertNext(11908.2609)
            .assertNext(11928.7941)
            .assertNext(11950.5749)
            .assertNext(11978.7156)
            .assertNext(12012.6402)
            .assertNext(12042.9401)
            .assertNext(12067.7868)
            .assertNext(12095.1832)
            .assertNext(12118.2508)
            .assertNext(12132.7027)
    }
}
