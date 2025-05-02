/// *
// * The MIT License (MIT)
// *
// * Copyright (c) 2017-2024 Ta4j Organization & respective
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
//package org.ta4j.core.criteria;
//
//import org.ta4j.core.api.bar.Bar;
//import org.ta4j.core.api.bar.BarSeries;
//import org.ta4j.core.backtest.Position;
//import org.ta4j.core.backtest.Trade.TradeType;
//import org.ta4j.core.backtest.TradingRecord;
//import org.ta4j.core.backtest.BacktestBarSeries;
//import org.ta4j.core.num.Num;
//import org.ta4j.core.num.NumFactoryProvider;
//
///**
// * Enter and hold criterion, returned in decimal format.
// *
// * <p>
// * Calculates the gross return (in percent) of an enter-and-hold strategy:
// *
// * <ul>
// * <li>For {@link #tradeType} = {@link TradeType#BUY}: Buy with the close price
// * of the first bar and sell with the close price of the last bar.
// * <li>For {@link #tradeType} = {@link TradeType#SELL}: Sell with the close
// * price of the first bar and buy with the close price of the last bar.
// * </ul>
// *
// * @see <a href=
// *     "http://en.wikipedia.org/wiki/Buy_and_hold">http://en.wikipedia.org/wiki/Buy_and_hold</a>
// */
//public class EnterAndHoldReturnCriterion implements AnalysisCriterion {
//
//  private final BarSeries series;
//  private final TradeType tradeType;
//
//
//  public static EnterAndHoldReturnCriterion buy(final BarSeries series) {
//    return new EnterAndHoldReturnCriterion(series, TradeType.BUY);
//  }
//
//
//  public static EnterAndHoldReturnCriterion sell(final BarSeries series) {
//    return new EnterAndHoldReturnCriterion(series, TradeType.SELL);
//  }
//
//
//  /**
//   * Constructor.
//   *
//   * @param series
//   * @param tradeType the {@link TradeType} used to open the position
//   */
//  EnterAndHoldReturnCriterion(final BarSeries series, final TradeType tradeType) {
//    this.series = series;
//    this.tradeType = tradeType;
//  }
//
//
//  @Override
//  public Num calculate(final Position position) {
//    final var beginIndex = position.getEntry().getExecutedBar();
//    final var backtestBarSeries = (BacktestBarSeries) this.series;  // TODO better typing
//    final var endIndex = backtestBarSeries.getLastBar();
//    return createEnterAndHoldTrade(backtestBarSeries, beginIndex, endIndex).getGrossReturn();
//  }
//
//
//  @Override
//  public Num calculate(final TradingRecord tradingRecord) {
//    final var backtestBarSeries = (BacktestBarSeries) this.series;  // TODO better typing
//    if (backtestBarSeries.isEmpty() || tradingRecord.isEmpty()) {
//      return NumFactoryProvider.getDefaultNumFactory().one();
//    }
//    final int beginIndex = tradingRecord.getStartIndex();
//    final int endIndex = tradingRecord.getEndIndex();
//    return createEnterAndHoldTrade(backtestBarSeries, this.series.getBar(beginIndex), this.series.getBar(endIndex)).getGrossReturn();
//  }
//
//
//  /** The higher the criterion value the better. */
//  @Override
//  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
//    return criterionValue1.isGreaterThan(criterionValue2);
//  }
//
//
//  private Position createEnterAndHoldTrade(final BacktestBarSeries series, final Bar begin, final Bar end) {
//    final var position = new Position(this.tradeType);
//    position.operate(begin, begin::closePrice, series.numFactory().one());
//    position.operate(end, end::closePrice, series.numFactory().one());
//    return position;
//  }
//}
