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
//import org.ta4j.core.backtest.Position;
//import org.ta4j.core.backtest.TradingRecord;
//import org.ta4j.core.indicators.candles.price.ClosePriceIndicator;
//import org.ta4j.core.num.Num;
//
///**
// * A stop-loss rule.
// *
// * <p>
// * Satisfied when the close price reaches the loss threshold.
// */
//public class StopLossRule extends AbstractRule {
//
//    /** The close price indicator. */
//    private final ClosePriceIndicator closePrice;
//
//    /** The loss percentage. */
//    private final Num lossPercentage;
//
//    /**
//     * Constructor.
//     *
//     * @param closePrice     the close price indicator
//     * @param lossPercentage the loss percentage
//     */
//    public StopLossRule(ClosePriceIndicator closePrice, Number lossPercentage) {
//        this(closePrice, closePrice.getBarSeries().numFactory().numOf(lossPercentage));
//    }
//
//    /**
//     * Constructor.
//     *
//     * @param closePrice     the close price indicator
//     * @param lossPercentage the loss percentage
//     */
//    public StopLossRule(ClosePriceIndicator closePrice, Num lossPercentage) {
//        this.closePrice = closePrice;
//        this.lossPercentage = lossPercentage;
//    }
//
//    /** This rule uses the {@code tradingRecord}. */
//    @Override
//    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
//        boolean satisfied = false;
//        // No trading history or no position opened, no loss
//        if (tradingRecord != null) {
//            Position currentPosition = tradingRecord.getCurrentPosition();
//            if (currentPosition.isOpened()) {
//
//                Num entryPrice = currentPosition.getEntry().getNetPrice();
//                Num currentPrice = closePrice.getValue(index);
//
//                if (currentPosition.getEntry().isBuy()) {
//                    satisfied = isBuyStopSatisfied(entryPrice, currentPrice);
//                } else {
//                    satisfied = isSellStopSatisfied(entryPrice, currentPrice);
//                }
//            }
//        }
//        traceIsSatisfied(index, satisfied);
//        return satisfied;
//    }
//
//    private boolean isBuyStopSatisfied(Num entryPrice, Num currentPrice) {
//        final var hundred = closePrice.getBarSeries().numFactory().hundred();
//        Num lossRatioThreshold = hundred.minus(lossPercentage).dividedBy(hundred);
//        Num threshold = entryPrice.multipliedBy(lossRatioThreshold);
//        return currentPrice.isLessThanOrEqual(threshold);
//    }
//
//    private boolean isSellStopSatisfied(Num entryPrice, Num currentPrice) {
//        final var hundred = closePrice.getBarSeries().numFactory().hundred();
//        Num lossRatioThreshold = hundred.plus(lossPercentage).dividedBy(hundred);
//        Num threshold = entryPrice.multipliedBy(lossRatioThreshold);
//        return currentPrice.isGreaterThanOrEqual(threshold);
//    }
//}
