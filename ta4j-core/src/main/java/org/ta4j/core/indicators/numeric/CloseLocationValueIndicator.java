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
//import org.ta4j.core.api.bar.Bar;
//import org.ta4j.core.api.bar.BarSeries;
//import org.ta4j.core.indicators.AbstractIndicator;
//import org.ta4j.core.num.Num;
//
///**
// * Close Location Value (CLV) indicator.
// *
// * @see <a href="http://www.investopedia.com/terms/c/close_location_value.asp">
// *      http://www.investopedia.com/terms/c/close_location_value.asp</a>
// */
//public class CloseLocationValueIndicator extends AbstractIndicator<Num> {
//
//    /**
//     * Constructor.
//     *
//     * @param series the bar series
//     */
//    public CloseLocationValueIndicator(BarSeries series) {
//        super(series);
//    }
//
//    protected Num calculate() {
//        final Bar bar = getBarSeries().getBar();
//        final Num low = bar.lowPrice();
//        final Num high = bar.highPrice();
//        final Num close = bar.closePrice();
//
//        final Num diffHighLow = high.minus(low);
//
//        return diffHighLow.isNaN() ? getBarSeries().numFactory().zero()
//                : ((close.minus(low)).minus(high.minus(close))).dividedBy(diffHighLow);
//    }
//
//
//    @Override
//    public Num getValue() {
//        return calculate();
//    }
//
//
//    /** @return {@code 0} */
//    @Override
//    public int getUnstableBars() {
//        return 0;
//    }
//}
