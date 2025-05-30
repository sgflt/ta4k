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
//package org.ta4j.core.backtest;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.ta4j.core.api.bar.BarSeries;
//import org.ta4j.core.backtest.Position;
//import org.ta4j.core.api.strategy.Strategy;
//import org.ta4j.core.backtest.Trade;
//import org.ta4j.core.backtest.Trade.TradeType;
//import org.ta4j.core.backtest.TradingRecord;
//import org.ta4j.core.indicators.AbstractIndicatorTest;
//import org.ta4j.core.mocks.MockBarSeriesBuilder;
//import org.ta4j.core.num.Num;
//import org.ta4j.core.num.NumFactory;
//import org.ta4j.core.rules.FixedRule;
//
//public class BarSeriesManagerTest extends AbstractIndicatorTest<BarSeries, Num> {
//
//    private BarSeries seriesForRun;
//
//    private BarSeriesManager manager;
//
//    private Strategy strategy;
//
//    private final Num HUNDRED = numOf(100);
//
//    public BarSeriesManagerTest(NumFactory numFactory) {
//        super(numFactory);
//    }
//
//    @Before
//    public void setUp() {
//
//        final DateTimeFormatter dtf = DateTimeFormatter.ISO_ZONED_DATE_TIME;
//        seriesForRun = new MockBarSeriesBuilder().withNumFactory(numFactory).build();
//
//        seriesForRun.barBuilder().endTime(ZonedDateTime.parse("2013-01-01T00:00:00-05:00", dtf)).closePrice(1d).onCandle();
//        seriesForRun.barBuilder().endTime(ZonedDateTime.parse("2013-08-01T00:00:00-05:00", dtf)).closePrice(2d).onCandle();
//        seriesForRun.barBuilder().endTime(ZonedDateTime.parse("2013-10-01T00:00:00-05:00", dtf)).closePrice(3d).onCandle();
//        seriesForRun.barBuilder().endTime(ZonedDateTime.parse("2013-12-01T00:00:00-05:00", dtf)).closePrice(4d).onCandle();
//        seriesForRun.barBuilder().endTime(ZonedDateTime.parse("2014-02-01T00:00:00-05:00", dtf)).closePrice(5d).onCandle();
//        seriesForRun.barBuilder().endTime(ZonedDateTime.parse("2015-01-01T00:00:00-05:00", dtf)).closePrice(6d).onCandle();
//        seriesForRun.barBuilder().endTime(ZonedDateTime.parse("2015-08-01T00:00:00-05:00", dtf)).closePrice(7d).onCandle();
//        seriesForRun.barBuilder().endTime(ZonedDateTime.parse("2015-10-01T00:00:00-05:00", dtf)).closePrice(8d).onCandle();
//        seriesForRun.barBuilder().endTime(ZonedDateTime.parse("2015-12-01T00:00:00-05:00", dtf)).closePrice(7d).onCandle();
//
//        manager = new BarSeriesManager(seriesForRun, new TradeOnCurrentCloseModel());
//
//        strategy = new BacktestStrategy(new FixedRule(0, 2, 3, 6), new FixedRule(1, 4, 7, 8));
//        strategy.setUnstableBars(2); // Strategy would need a real test class
//    }
//
//    @Test
//    public void runOnWholeSeries() {
//        var series = new MockBarSeriesBuilder().withNumFactory(numFactory)
//                .withCandlePrices(20d, 40d, 60d, 10d, 30d, 50d, 0d, 20d, 40d)
//                .build();
//        manager = new BarSeriesManager(series, new TradeOnCurrentCloseModel());
//        List<Position> allPositions = manager.run(strategy).getPositions();
//        assertEquals(2, allPositions.size());
//    }
//
//    @Test
//    public void runOnWholeSeriesWithAmount() {
//        var series = new MockBarSeriesBuilder().withNumFactory(numFactory)
//                .withCandlePrices(20d, 40d, 60d, 10d, 30d, 50d, 0d, 20d, 40d)
//                .build();
//        manager = new BarSeriesManager(series, new TradeOnCurrentCloseModel());
//        List<Position> allPositions = manager.run(strategy, TradeType.BUY, HUNDRED).getPositions();
//
//        assertEquals(2, allPositions.size());
//        assertEquals(HUNDRED, allPositions.get(0).getEntry().getAmount());
//        assertEquals(HUNDRED, allPositions.get(1).getEntry().getAmount());
//
//    }
//
//    @Test
//    public void runOnSeries() {
//        List<Position> positions = manager.run(strategy).getPositions();
//        assertEquals(2, positions.size());
//
//        assertEquals(Trade.buyAt(2, seriesForRun.getBar(2).closePrice(), numOf(1)), positions.get(0).getEntry());
//        assertEquals(Trade.sellAt(4, seriesForRun.getBar(4).closePrice(), numOf(1)), positions.get(0).getExit());
//
//        assertEquals(Trade.buyAt(6, seriesForRun.getBar(6).closePrice(), numOf(1)), positions.get(1).getEntry());
//        assertEquals(Trade.sellAt(7, seriesForRun.getBar(7).closePrice(), numOf(1)), positions.get(1).getExit());
//    }
//
//    @Test
//    public void runWithOpenEntryBuyLeft() {
//        Strategy aStrategy = new BacktestStrategy(new FixedRule(1), new FixedRule(3));
//        List<Position> positions = manager.run(aStrategy, 0, 3).getPositions();
//        assertEquals(1, positions.size());
//
//        assertEquals(Trade.buyAt(1, seriesForRun.getBar(1).closePrice(), numOf(1)), positions.get(0).getEntry());
//        assertEquals(Trade.sellAt(3, seriesForRun.getBar(3).closePrice(), numOf(1)), positions.get(0).getExit());
//    }
//
//    @Test
//    public void runWithOpenEntrySellLeft() {
//        Strategy aStrategy = new BacktestStrategy(new FixedRule(1), new FixedRule(3));
//        List<Position> positions = manager.run(aStrategy, TradeType.SELL, 0, 3).getPositions();
//        assertEquals(1, positions.size());
//
//        assertEquals(Trade.sellAt(1, seriesForRun.getBar(1).closePrice(), numOf(1)), positions.get(0).getEntry());
//        assertEquals(Trade.buyAt(3, seriesForRun.getBar(3).closePrice(), numOf(1)), positions.get(0).getExit());
//    }
//
//    @Test
//    public void runBetweenIndexes() {
//
//        // only 1 entry happened within [0-3]
//        TradingRecord tradingRecord = manager.run(strategy, 0, 3);
//        List<Position> positions = tradingRecord.getPositions();
//        assertEquals(0, tradingRecord.getPositions().size());
//        assertEquals(2, tradingRecord.getCurrentPosition().getEntry().getIndex());
//
//        // 1 entry and 1 exit happened within [0-4]
//        tradingRecord = manager.run(strategy, 0, 4);
//        positions = tradingRecord.getPositions();
//        assertEquals(1, positions.size());
//        assertEquals(Trade.buyAt(2, seriesForRun.getBar(2).closePrice(), numOf(1)), positions.get(0).getEntry());
//        assertEquals(Trade.sellAt(4, seriesForRun.getBar(4).closePrice(), numOf(1)), positions.get(0).getExit());
//
//        // no trades happened within [4-4]
//        tradingRecord = manager.run(strategy, 4, 4);
//        positions = tradingRecord.getPositions();
//        assertTrue(positions.isEmpty());
//
//        // 1 entry and 1 exit happened within [5-8]
//        tradingRecord = manager.run(strategy, 5, 8);
//        positions = tradingRecord.getPositions();
//        assertEquals(1, positions.size());
//        assertEquals(Trade.buyAt(6, seriesForRun.getBar(6).closePrice(), numOf(1)), positions.get(0).getEntry());
//        assertEquals(Trade.sellAt(7, seriesForRun.getBar(7).closePrice(), numOf(1)), positions.get(0).getExit());
//    }
//
//    @Test
//    public void runOnSeriesSlices() {
//        var series = new MockBarSeriesBuilder().withNumFactory(numFactory).build();
//
//        series.barBuilder().closePrice(1d).onCandle();
//        series.barBuilder().closePrice(2d).onCandle();
//        series.barBuilder().closePrice(3d).onCandle();
//        series.barBuilder().closePrice(4d).onCandle();
//        series.barBuilder().closePrice(5d).onCandle();
//        series.barBuilder().closePrice(6d).onCandle();
//        series.barBuilder().closePrice(7d).onCandle();
//        series.barBuilder().closePrice(8d).onCandle();
//        series.barBuilder().closePrice(9d).onCandle();
//        series.barBuilder().closePrice(10d).onCandle();
//
//        manager = new BarSeriesManager(series, new TradeOnCurrentCloseModel());
//
//        Strategy aStrategy = new BacktestStrategy(new FixedRule(0, 3, 5, 7), new FixedRule(2, 4, 6, 9));
//
//        // only 1 entry happened within [0-1]
//        TradingRecord tradingRecord = manager.run(aStrategy, 0, 1);
//        List<Position> positions = tradingRecord.getPositions();
//        assertEquals(0, positions.size());
//        assertEquals(0, tradingRecord.getCurrentPosition().getEntry().getIndex());
//
//        // only 1 entry happened within [2-3]
//        tradingRecord = manager.run(aStrategy, 2, 3);
//        positions = tradingRecord.getPositions();
//        assertEquals(0, positions.size());
//        assertEquals(3, tradingRecord.getCurrentPosition().getEntry().getIndex());
//
//        // 1 entry and 1 exit happened within [4-6]
//        positions = manager.run(aStrategy, 4, 6).getPositions();
//        assertEquals(1, positions.size());
//        assertEquals(Trade.buyAt(5, series.getBar(5).closePrice(), numOf(1)), positions.get(0).getEntry());
//        assertEquals(Trade.sellAt(6, series.getBar(6).closePrice(), numOf(1)), positions.get(0).getExit());
//
//        // 1 entry happened within [7-7]
//        tradingRecord = manager.run(aStrategy, 7, 7);
//        positions = tradingRecord.getPositions();
//        assertEquals(0, positions.size());
//        assertEquals(7, tradingRecord.getCurrentPosition().getEntry().getIndex());
//
//        // no trade happened within [8-8]
//        positions = manager.run(aStrategy, 8, 8).getPositions();
//        assertTrue(positions.isEmpty());
//
//        // no trade happened within [9-9]
//        positions = manager.run(aStrategy, 9, 9).getPositions();
//        assertTrue(positions.isEmpty());
//    }
//}
