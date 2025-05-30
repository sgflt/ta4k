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
//package ta4jexamples;
//
//import org.ta4j.core.backtest.criteria.AnalysisCriterion;
//import org.ta4j.core.backtest.criteria.AnalysisCriterion.PositionFilter;
//import org.ta4j.core.backtest.strategy.BacktestStrategy;
//import org.ta4j.core.api.bar.BarSeries;
//import org.ta4j.core.api.strategy.Rule;
//import org.ta4j.core.backtest.TradingRecord;
//import org.ta4j.core.criteria.PositionsRatioCriterion;
//import org.ta4j.core.criteria.ReturnOverMaxDrawdownCriterion;
//import org.ta4j.core.criteria.VersusEnterAndHoldCriterion;
//import org.ta4j.core.criteria.pnl.ReturnCriterion;
//import org.ta4j.core.indicators.numeric.average.SMAIndicator;
//import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
//import org.ta4j.core.num.Num;
//import org.ta4j.core.rules.CrossedDownIndicatorRule;
//import org.ta4j.core.rules.CrossedUpIndicatorRule;
//import org.ta4j.core.rules.StopGainRule;
//import org.ta4j.core.rules.StopLossRule;
//
//import ta4jexamples.loaders.CsvTradesLoader;
//
///**
// * Quickstart for ta4j.
// *
// * Global example.
// */
//public class Quickstart {
//
//    public static void main(String[] args) {
//
//        // Getting a bar series (from any provider: CSV, web service, etc.)
//        BarSeries series = CsvTradesLoader.loadBitstampSeries();
//
//        // Getting the close price of the bars
//        Num firstClosePrice = series.getBar(0).closePrice();
//        System.out.println("First close price: " + firstClosePrice.doubleValue());
//        // Or within an indicator:
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        // Here is the same close price:
//        System.out.println(firstClosePrice.isEqual(closePrice.getValue(0))); // equal to firstClosePrice
//
//        // Getting the simple moving average (SMA) of the close price over the last 5
//        // bars
//        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
//        // Here is the 5-bars-SMA value at the 42nd index
//        System.out.println("5-bars-SMA value at the 42nd index: " + shortSma.getValue(42).doubleValue());
//
//        // Getting a longer SMA (e.g. over the 30 last bars)
//        SMAIndicator longSma = new SMAIndicator(closePrice, 30);
//
//        // Ok, now let's building our trading rules!
//
//        // Buying rules
//        // We want to buy:
//        // - if the 5-bars SMA crosses over 30-bars SMA
//        // - or if the price goes below a defined price (e.g $800.00)
//        Rule buyingRule = new CrossedUpIndicatorRule(shortSma, longSma)
//                .or(new CrossedDownIndicatorRule(closePrice, 800));
//
//        // Selling rules
//        // We want to sell:
//        // - if the 5-bars SMA crosses under 30-bars SMA
//        // - or if the price loses more than 3%
//        // - or if the price earns more than 2%
//        Rule sellingRule = new CrossedDownIndicatorRule(shortSma, longSma)
//                .or(new StopLossRule(closePrice, series.numFactory().numOf(3)))
//                .or(new StopGainRule(closePrice, series.numFactory().numOf(2)));
//
//        // Running our juicy trading strategy...
//        BarSeriesManager seriesManager = new BarSeriesManager(series);
//        TradingRecord tradingRecord = seriesManager.run(new BacktestStrategy(buyingRule, sellingRule));
//        System.out.println("Number of positions for our strategy: " + tradingRecord.getPositionCount());
//
//        // Analysis
//
//        // Getting the winning positions ratio
//        AnalysisCriterion winningPositionsRatio = new PositionsRatioCriterion(PositionFilter.PROFIT);
//        System.out.println("Winning positions ratio: " + winningPositionsRatio.calculate(series, tradingRecord));
//        // Getting a risk-reward ratio
//        AnalysisCriterion romad = new ReturnOverMaxDrawdownCriterion();
//        System.out.println("Return over Max Drawdown: " + romad.calculate(series, tradingRecord));
//
//        // Total return of our strategy vs total return of a buy-and-hold strategy
//        AnalysisCriterion vsBuyAndHold = new VersusEnterAndHoldCriterion(new ReturnCriterion());
//        System.out.println("Our return vs buy-and-hold return: " + vsBuyAndHold.calculate(series, tradingRecord));
//
//        // Your turn!
//    }
//}
