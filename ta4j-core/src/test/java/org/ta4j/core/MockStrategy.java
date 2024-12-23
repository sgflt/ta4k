//package org.ta4j.core;
//
//import org.ta4j.core.backtest.strategy.BacktestStrategy;
//import org.ta4j.core.indicators.Indicator;
//import org.ta4j.core.indicators.IndicatorContext;
//
// TODO delete?
/// **
// * @author Lukáš Kvídera
// */
//public class MockStrategy extends BacktestStrategy {
//  public MockStrategy(final IndicatorContext indicatorContext) {
//    super(
//        new Strategy() {
//          @Override
//          public String getName() {
//            return "mock strategy";
//          }
//
//
//          @Override
//          public Rule getEntryRule() {
//            return Rule.NOOP;
//          }
//
//
//          @Override
//          public Rule getExitRule() {
//            return Rule.NOOP;
//          }
//
//
//          @Override
//          public boolean isStable() {
//            return indicatorContext.isStable();
//          }
//        },
//        null
//    );
//  }
//
//
//  public MockStrategy(final Indicator<?>... mockIndicators) {
//    this(IndicatorContext.of(mockIndicators));
//  }
//}
