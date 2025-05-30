/// **
// * The MIT License (MIT)
// *
// * Copyright (c) 2017-2023 Ta4j Organization & respective
// * authors (see AUTHORS)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy of
// * this software and associated documentation files (the "Software"), to deal in
// * the Software without restriction, including without limitation the rights to
// * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
// * the Software, and to permit persons to whom the Software is furnished to do so,
// * subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// */
//package org.ta4j.core;
//
//import static org.junit.Assert.assertEquals;
//import static org.ta4j.core.TestUtils.assertNumEquals;
//
//import java.math.BigDecimal;
//import java.time.Duration;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//
//import org.junit.Test;
//import org.ta4j.core.indicators.AbstractIndicatorTest;
//import org.ta4j.core.mocks.MockBarSeriesBuilder;
//import org.ta4j.core.num.Num;
//import org.ta4j.core.num.NumFactory;
//
//public class BacktestBarConvertibleBuilderTest {
//
//  public BacktestBarConvertibleBuilderTest(final NumFactory numFactory) {
//    super(numFactory);
//  }
//
//
//  @Test
//  public void testBuildBigDecimal() {
//
//    final var beginTime = ZonedDateTime.of(2014, 6, 25, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
//    final var endTime = ZonedDateTime.of(2014, 6, 25, 1, 0, 0, 0, ZoneId.systemDefault()).toInstant();
//    final var duration = Duration.between(beginTime, endTime);
//
//    final var series = new MockBarSeriesBuilder().withNumFactory(this.numFactory).build();
//    final var bar = series.barBuilder()
//        .timePeriod(duration)
//        .endTime(endTime)
//        .openPrice(BigDecimal.valueOf(101.0))
//        .highPrice(BigDecimal.valueOf(103))
//        .lowPrice(BigDecimal.valueOf(100))
//        .closePrice(BigDecimal.valueOf(102))
//        .trades(4)
//        .volume(BigDecimal.valueOf(40))
//        .amount(BigDecimal.valueOf(4020))
//        .build();
//
//    assertEquals(duration, bar.timePeriod());
//    assertEquals(beginTime, bar.beginTime());
//    assertEquals(endTime, bar.endTime());
//    assertNumEquals(numOf(101.0), bar.openPrice());
//    assertNumEquals(numOf(103), bar.highPrice());
//    assertNumEquals(numOf(100), bar.lowPrice());
//    assertNumEquals(numOf(102), bar.closePrice());
//    assertEquals(4, bar.getTrades());
//    assertNumEquals(numOf(40), bar.volume());
//    assertNumEquals(numOf(4020), bar.getAmount());
//  }
//}
