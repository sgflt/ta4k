///**
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
//package org.ta4j.core.indicators.helpers;
//
//import static org.junit.Assert.assertEquals;
//
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
//
//import org.junit.Test;
//import org.ta4j.core.api.bar.Bar;
//import org.ta4j.core.api.bar.BarSeries;
//import org.ta4j.core.api.Indicator;
//import org.ta4j.core.indicators.AbstractIndicatorTest;
//import org.ta4j.core.mocks.MockBarSeriesBuilder;
//import org.ta4j.core.num.Num;
//import org.ta4j.core.num.NumFactory;
//
//public class DateTimeIndicatorTest extends AbstractIndicatorTest<Indicator<Num>, Num> {
//
//    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;
//
//    public DateTimeIndicatorTest(NumFactory numFactory) {
//        super(numFactory);
//    }
//
//    @Test
//    public void test() {
//        ZonedDateTime expectedZonedDateTime = ZonedDateTime.parse("2019-09-17T00:04:00-00:00", DATE_TIME_FORMATTER);
//        BarSeries series = new MockBarSeriesBuilder().withNumFactory(numFactory).build();
//        series.barBuilder().endTime(expectedZonedDateTime).onCandle();
//        DateTimeIndicator dateTimeIndicator = new DateTimeIndicator(series, Bar::endTime);
//        assertEquals(expectedZonedDateTime, dateTimeIndicator.getValue(0));
//    }
//}
