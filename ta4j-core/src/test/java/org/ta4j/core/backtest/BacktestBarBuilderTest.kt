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
package org.ta4j.core.backtest

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.num.NumFactory

class BacktestBarBuilderTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun buildBar(numFactory: NumFactory) {
        val beginTime = ZonedDateTime.of(2014, 6, 25, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant()
        val endTime = ZonedDateTime.of(2014, 6, 25, 1, 0, 0, 0, ZoneId.systemDefault()).toInstant()
        val duration = Duration.between(beginTime, endTime)

        val bar = BacktestBarBuilder(BacktestBarSeriesBuilder().withNumFactory(numFactory).build())
            .startTime(beginTime)
            .endTime(endTime)
            .openPrice(numFactory.numOf(101))
            .highPrice(numFactory.numOf(103))
            .lowPrice(numFactory.numOf(100))
            .closePrice(numFactory.numOf(102))
            .trades(4)
            .volume(numFactory.numOf(40))
            .amount(numFactory.numOf(4020))
            .build()

        assertThat(bar.timePeriod).isEqualTo(duration)
        assertThat(bar.beginTime).isEqualTo(beginTime)
        assertThat(bar.endTime).isEqualTo(endTime)
        assertThat(bar.openPrice).isEqualTo(numFactory.numOf(101))
        assertThat(bar.highPrice).isEqualTo(numFactory.numOf(103))
        assertThat(bar.lowPrice).isEqualTo(numFactory.numOf(100))
        assertThat(bar.closePrice).isEqualTo(numFactory.numOf(102))
        assertThat(bar.trades).isEqualTo(4)
        assertThat(bar.volume).isEqualTo(numFactory.numOf(40))
        assertThat(bar.amount).isEqualTo(numFactory.numOf(4020))
    }
}
