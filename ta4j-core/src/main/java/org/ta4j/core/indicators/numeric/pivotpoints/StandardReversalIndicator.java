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
//package org.ta4j.core.indicators.pivotpoints;
//
//import static org.ta4j.core.num.NaN.NaN;
//
//import java.util.List;
//
//import org.ta4j.core.api.bar.Bar;
//import org.ta4j.core.num.Num;
//
///**
// * Pivot Reversal Indicator.
// *
// * @see <a href=
// *      "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:pivot_points">
// *      http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:pivot_points</a>
// */
//public class StandardReversalIndicator extends RecursiveCachedIndicator<Num> {
//
//    private final PivotPointIndicator pivotPointIndicator;
//    private final PivotLevel level;
//    private final Num two;
//
//    /**
//     * Constructor.
//     *
//     * Calculates the (standard) reversal for the corresponding pivot level
//     *
//     * @param pivotPointIndicator the {@link PivotPointIndicator} for this reversal
//     * @param level               the {@link PivotLevel} for this reversal
//     */
//    public StandardReversalIndicator(PivotPointIndicator pivotPointIndicator, PivotLevel level) {
//        super(pivotPointIndicator);
//        this.pivotPointIndicator = pivotPointIndicator;
//        this.level = level;
//        this.two = pivotPointIndicator.getBarSeries().numFactory().numOf(2);
//    }
//
//    @Override
//    protected Num calculate(int index) {
//        List<Integer> barsOfPreviousPeriod = pivotPointIndicator.getBarsOfPreviousPeriod(index);
//        if (barsOfPreviousPeriod.isEmpty()) {
//            return NaN;
//        }
//        switch (level) {
//        case RESISTANCE_3:
//            return calculateR3(barsOfPreviousPeriod, index);
//        case RESISTANCE_2:
//            return calculateR2(barsOfPreviousPeriod, index);
//        case RESISTANCE_1:
//            return calculateR1(barsOfPreviousPeriod, index);
//        case SUPPORT_1:
//            return calculateS1(barsOfPreviousPeriod, index);
//        case SUPPORT_2:
//            return calculateS2(barsOfPreviousPeriod, index);
//        case SUPPORT_3:
//            return calculateS3(barsOfPreviousPeriod, index);
//        default:
//            return NaN;
//        }
//    }
//
//    @Override
//    public int getUnstableBars() {
//        return 0;
//    }
//
//    private Num calculateR3(List<Integer> barsOfPreviousPeriod, int index) {
//        Bar bar = getBarSeries().getBar(barsOfPreviousPeriod.get(0));
//        Num low = bar.lowPrice();
//        Num high = bar.highPrice();
//        for (int i : barsOfPreviousPeriod) {
//            Bar iBar = getBarSeries().getBar(i);
//            low = iBar.lowPrice().min(low);
//            high = iBar.highPrice().max(high);
//        }
//        return high.plus(two.multipliedBy((pivotPointIndicator.getValue(index).minus(low))));
//    }
//
//    private Num calculateR2(List<Integer> barsOfPreviousPeriod, int index) {
//        Bar bar = getBarSeries().getBar(barsOfPreviousPeriod.get(0));
//        Num low = bar.lowPrice();
//        Num high = bar.highPrice();
//        for (int i : barsOfPreviousPeriod) {
//            Bar iBar = getBarSeries().getBar(i);
//            low = iBar.lowPrice().min(low);
//            high = iBar.highPrice().max(high);
//        }
//        return pivotPointIndicator.getValue(index).plus((high.minus(low)));
//    }
//
//    private Num calculateR1(List<Integer> barsOfPreviousPeriod, int index) {
//        Num low = getBarSeries().getBar(barsOfPreviousPeriod.get(0)).lowPrice();
//        for (int i : barsOfPreviousPeriod) {
//            low = (getBarSeries().getBar(i).lowPrice()).min(low);
//        }
//        return two.multipliedBy(pivotPointIndicator.getValue(index)).minus(low);
//    }
//
//    private Num calculateS1(List<Integer> barsOfPreviousPeriod, int index) {
//        Num high = getBarSeries().getBar(barsOfPreviousPeriod.get(0)).highPrice();
//        for (int i : barsOfPreviousPeriod) {
//            high = (getBarSeries().getBar(i).highPrice()).max(high);
//        }
//        return two.multipliedBy(pivotPointIndicator.getValue(index)).minus(high);
//    }
//
//    private Num calculateS2(List<Integer> barsOfPreviousPeriod, int index) {
//        Bar bar = getBarSeries().getBar(barsOfPreviousPeriod.get(0));
//        Num high = bar.highPrice();
//        Num low = bar.lowPrice();
//        for (int i : barsOfPreviousPeriod) {
//            Bar iBar = getBarSeries().getBar(i);
//            high = iBar.highPrice().max(high);
//            low = iBar.lowPrice().min(low);
//        }
//        return pivotPointIndicator.getValue(index).minus((high.minus(low)));
//    }
//
//    private Num calculateS3(List<Integer> barsOfPreviousPeriod, int index) {
//        Bar bar = getBarSeries().getBar(barsOfPreviousPeriod.get(0));
//        Num high = bar.highPrice();
//        Num low = bar.lowPrice();
//        for (int i : barsOfPreviousPeriod) {
//            Bar iBar = getBarSeries().getBar(i);
//            high = iBar.highPrice().max(high);
//            low = iBar.lowPrice().min(low);
//        }
//        return low.minus(two.multipliedBy((high.minus(pivotPointIndicator.getValue(index)))));
//    }
//}
