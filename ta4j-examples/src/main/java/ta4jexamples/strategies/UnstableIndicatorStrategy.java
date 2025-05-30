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
//package ta4jexamples.strategies;
//
//import java.time.Duration;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.util.stream.Stream;
//
//import org.ta4j.core.api.bar.BarSeries;
//import org.ta4j.core.backtest.BacktestBarSeriesBuilder;
//import org.ta4j.core.backtest.strategy.BacktestStrategy;
//import org.ta4j.core.api.Indicator;
//import org.ta4j.core.api.strategy.Rule;
//import org.ta4j.core.api.strategy.Strategy;
//import org.ta4j.core.backtest.TradingRecord;
//import org.ta4j.core.indicators.numeric.average.SMAIndicator;
//import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
//import org.ta4j.core.indicators.helpers.UnstableIndicator;
//import org.ta4j.core.num.Num;
//import org.ta4j.core.rules.CrossedDownIndicatorRule;
//import org.ta4j.core.rules.CrossedUpIndicatorRule;
//
//public class UnstableIndicatorStrategy {
//
//    public static final Duration MINUTE = Duration.ofMinutes(1);
//    public static final ZonedDateTime TIME = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
//
//    public static Strategy buildStrategy(BarSeries series) {
//        ClosePriceIndicator close = new ClosePriceIndicator(series);
//        int smaPeriod = 3;
//        Indicator<Num> sma = new UnstableIndicator(new SMAIndicator(close, smaPeriod), smaPeriod - 1);
//
//        Rule entryRule = new CrossedUpIndicatorRule(close, sma);
//        Rule exitRule = new CrossedDownIndicatorRule(close, sma);
//
//        BacktestStrategy strategy = new BacktestStrategy(entryRule, exitRule);
//        strategy.setUnstableBars(3);
//        return strategy;
//    }
//
//    public static void main(String[] args) {
//        inappropriateTrade();
//        appropriateTrade();
//    }
//
//    public static void inappropriateTrade() {
//        // Should not trade
//        test("Inappropriate trade", Stream.of(10d, 2d, 6d, 16d, 8d));
//    }
//
//    public static void appropriateTrade() {
//        // Should trade
//        test("Appropriate trade", Stream.of(10d, 8d, 6d, 16d, 8d));
//    }
//
//    public static void test(String name, Stream<Double> closePrices) {
//        // Getting the bar series
//        BarSeries series = new BacktestBarSeriesBuilder().build();
//
//        closePrices.forEach(close -> series.barBuilder()
//                .timePeriod(MINUTE)
//                .endTime(TIME)
//                .openPrice(0)
//                .closePrice(close)
//                .highPrice(0)
//                .lowPrice(0)
//                .onCandle());
//
//        // Building the trading strategy
//        Strategy strategy = buildStrategy(series);
//
//        // Running the strategy
//        BarSeriesManager seriesManager = new BarSeriesManager(series);
//        TradingRecord tradingRecord = seriesManager.run(strategy);
//
//        System.out.println(name + " " + tradingRecord.getPositions());
//    }
//
//}
