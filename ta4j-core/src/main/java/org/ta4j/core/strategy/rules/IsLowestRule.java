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
//package org.ta4j.core.rules;
//
//import org.ta4j.core.api.Indicator;
//import org.ta4j.core.backtest.TradingRecord;
//import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator;
//import org.ta4j.core.num.Num;
//
///**
// * Satisfied when the value of the {@link Indicator indicator} is the lowest
// * within the {@code barCount}.
// */
//public class IsLowestRule extends AbstractRule {
//
//    /** The actual indicator. */
//    private final Indicator<Num> ref;
//
//    /** The barCount. */
//    private final int barCount;
//
//    /**
//     * Constructor.
//     *
//     * @param ref      the indicator
//     * @param barCount the time frame
//     */
//    public IsLowestRule(Indicator<Num> ref, int barCount) {
//        this.ref = ref;
//        this.barCount = barCount;
//    }
//
//    /** This rule does not use the {@code tradingRecord}. */
//    @Override
//    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
//        LowestValueIndicator lowest = new LowestValueIndicator(ref, barCount);
//        Num lowestVal = lowest.getValue(index);
//        Num refVal = ref.getValue(index);
//
//        final boolean satisfied = !refVal.isNaN() && !lowestVal.isNaN() && refVal.equals(lowestVal);
//        traceIsSatisfied(index, satisfied);
//        return satisfied;
//    }
//}
