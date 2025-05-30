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
//import java.time.ZonedDateTime;
//import java.util.function.Function;
//
//import org.ta4j.core.api.bar.Bar;
//import org.ta4j.core.api.bar.BarSeries;
//import org.ta4j.core.indicators.AbstractIndicator;
//
///**
// * DateTime indicator.
// *
// * <p>
// * Returns a {@link ZonedDateTime} of (or for) a bar.
// */
//public class DateTimeIndicator extends AbstractIndicator<ZonedDateTime> {
//
//    private final Function<Bar, ZonedDateTime> action;
//
//    /**
//     * Constructor to return {@link Bar#beginTime()} of a bar.
//     *
//     * @param barSeries the bar series
//     */
//    public DateTimeIndicator(BarSeries barSeries) {
//        this(barSeries, Bar::beginTime);
//    }
//
//    /**
//     * Constructor.
//     *
//     * @param barSeries the bar series
//     * @param action    the action
//     */
//    public DateTimeIndicator(BarSeries barSeries, Function<Bar, ZonedDateTime> action) {
//        super(barSeries);
//        this.action = action;
//    }
//
//    protected ZonedDateTime calculate() {
//        Bar bar = getBarSeries().getBar();
//        return this.action.apply(bar);
//    }
//
//
//    @Override
//    public ZonedDateTime getValue() {
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
