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
//package org.ta4j.core.indicators;
//
//import static org.ta4j.core.TestUtils.assertNumEquals;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.ta4j.core.api.bar.BarSeries;
//import org.ta4j.core.indicators.numeric.average.SMAIndicator;
//import org.ta4j.core.indicators.candles.price.ClosePriceIndicator;
//import org.ta4j.core.mocks.MockBarSeriesBuilder;
//import org.ta4j.core.num.Num;
//import org.ta4j.core.num.NumFactory;
//
//public class DistanceFromMAIndicatorTest extends AbstractIndicatorTest<Indicator<Num>, Num> {
//    private BarSeries data;
//
//    public DistanceFromMAIndicatorTest(NumFactory numFactory) {
//        super(numFactory);
//    }
//
//    @Before
//    public void setUp() {
//        data = new MockBarSeriesBuilder().withNumFactory(numFactory)
//                .withCandlePrices(10, 15, 20, 18, 17, 18, 15, 12, 10, 8, 5, 2)
//                .build();
//    }
//
//    @Test
//    public void DistanceFromMovingAverageTest() {
//        SMAIndicator sma = new SMAIndicator(new ClosePriceIndicator(data), 3);
//        DistanceFromMAIndicator distanceFromMAIndicator = new DistanceFromMAIndicator(data, sma);
//        assertNumEquals(0.3333, distanceFromMAIndicator.getValue(2));
//        assertNumEquals(0.01886792452830182, distanceFromMAIndicator.getValue(5));
//        assertNumEquals(-0.1, distanceFromMAIndicator.getValue(6));
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void DistanceFromIllegalMovingAverage() {
//        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(data);
//        new DistanceFromMAIndicator(data, closePriceIndicator);
//    }
//}
