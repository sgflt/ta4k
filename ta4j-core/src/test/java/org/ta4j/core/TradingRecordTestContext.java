package org.ta4j.core;

import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import org.ta4j.core.backtest.BacktestBarSeries;
import org.ta4j.core.backtest.TradingRecord;
import org.ta4j.core.backtest.analysis.cost.CostModel;
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel;
import org.ta4j.core.backtest.criteria.AnalysisCriterion;
import org.ta4j.core.backtest.strategy.BackTestTradingRecord;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * @author Lukáš Kvídera
 */
public class TradingRecordTestContext {

  private int simulationTime;
  private TradeType tradeType = TradeType.BUY;
  private BackTestTradingRecord tradingRecord;
  private AnalysisCriterion criterion;
  private CostModel transactionCostModel = ZeroCostModel.INSTANCE;
  private CostModel holdingCostModel = ZeroCostModel.INSTANCE;
  private MarketEventTestContext marketEvenTestContext;
  private NumFactory numFactory = NumFactoryProvider.getDefaultNumFactory();
  private ChronoUnit resolution = ChronoUnit.DAYS;


  public TradingRecordTestContext() {
    reinitalizeTradingRecord();
  }


  public TradingRecordTestContext(final MarketEventTestContext marketEventTestContext) {
    this.marketEvenTestContext = marketEventTestContext;
    withNumFactory(marketEventTestContext.getBarSeries().getNumFactory());
    reinitalizeTradingRecord();
  }


  public TradingRecordTestContext withTradeType(final TradeType tradeType) {
    this.tradeType = tradeType;
    reinitalizeTradingRecord();
    return this;
  }


  public TradingRecordTestContext withNumFactory(final NumFactory numFactory) {
    this.numFactory = numFactory;
    NumFactoryProvider.setDefaultNumFactory(numFactory);
    reinitalizeTradingRecord();
    return this;
  }


  private void reinitalizeTradingRecord() {
    this.tradingRecord = new BackTestTradingRecord(
        this.tradeType,
        "",
        this.transactionCostModel,
        this.holdingCostModel,
        this.numFactory
    );

    if (this.marketEvenTestContext != null) {
      this.marketEvenTestContext.withBarListener(this.tradingRecord);
    }
  }


  public TradingRecordTestContext withCriterion(final AnalysisCriterion criterion) {
    this.criterion = criterion;
    return this;
  }


  public TradingRecordTestContext withSeriesRelatedCriterion(final Function<BacktestBarSeries, AnalysisCriterion> criterionFactory) {
    this.criterion = criterionFactory.apply(this.marketEvenTestContext.getBarSeries());
    return this;
  }


  public TradingRecordTestContext withTransactionCostModel(final CostModel transactionCostModel) {
    this.transactionCostModel = transactionCostModel;
    reinitalizeTradingRecord();
    return this;
  }


  public TradingRecordTestContext withHoldingCostModel(final CostModel holdingCostModel) {
    this.holdingCostModel = holdingCostModel;
    reinitalizeTradingRecord();
    return this;
  }


  public TradingRecord getTradingRecord() {
    return this.tradingRecord;
  }


  public void assertResults(final double expected) {
    assertNumEquals(expected, this.criterion.calculate(this.tradingRecord));
  }


  public Operation enter(final double amount) {
    return new EnterOperation(amount);
  }


  public Operation exit(final double amount) {
    return new ExitOperation(amount);
  }


  private NumFactory getNumFactory() {
    return this.numFactory;
  }


  public BacktestBarSeries getBarSeries() {
    return this.marketEvenTestContext.getBarSeries();
  }


  public TradingRecordTestContext forwardTime(final int countOfBars) {
    if (this.marketEvenTestContext != null) {
      this.marketEvenTestContext.fastForward(countOfBars);
    } else {
      this.simulationTime += countOfBars;
    }


    return this;
  }


  public TradingRecordTestContext withResolution(final ChronoUnit resolution) {
    this.resolution = resolution;
    return this;
  }


  public interface Operation {
    /**
     * This method executes operation relatively to current time.
     *
     * 0 enter immediatelly at current bar
     * 1 means one bar is replayed, enter at next bar
     *
     * {@code
     * .enter(1).after(1) // wait until next bar
     * .exit(1).after(2) // wait two bars, exit at the second
     * }
     *
     * @param countOfBars numbered from 1
     */
    TradingRecordTestContext after(int countOfBars);

    /**
     * Executes operation as soon as possible. Usually at next bar.
     */
    default TradingRecordTestContext asap() {
      return after(1);
    }

    /**
     * This method does not care about time, just mocks entry at some price.
     * Also does not refresh history of positions price.
     *
     * @param price at which we enter
     *
     * @return this
     */
    TradingRecordTestContext at(double price);
  }

  public class EnterOperation implements Operation {
    private final double amount;


    public EnterOperation(final double amount) {
      this.amount = amount;
    }


    public TradingRecordTestContext after(final int countOfBars) {
      TradingRecordTestContext.this.marketEvenTestContext.fastForward(countOfBars);

      final var bar = TradingRecordTestContext.this.marketEvenTestContext.getBarSeries().getBar();
      TradingRecordTestContext.this.tradingRecord.enter(
          bar.getEndTime(),
          bar.getClosePrice(),
          getNumFactory().numOf(this.amount)
      );

      // ad bar to positions history
      TradingRecordTestContext.this.tradingRecord.onBar(bar);

      return TradingRecordTestContext.this;
    }


    @Override
    public TradingRecordTestContext at(final double price) {
      TradingRecordTestContext.this.tradingRecord.enter(
          getSimulatedTime(),
          getNumFactory().numOf(price),
          getNumFactory().numOf(this.amount)
      );

      return TradingRecordTestContext.this;
    }
  }

  public class ExitOperation implements Operation {
    private final double amount;


    public ExitOperation(final double amount) {
      this.amount = amount;
    }


    public TradingRecordTestContext after(final int orderOfReceivedCandle) {
      TradingRecordTestContext.this.marketEvenTestContext.fastForward(orderOfReceivedCandle);

      final var bar = TradingRecordTestContext.this.marketEvenTestContext.getBarSeries().getBar();
      TradingRecordTestContext.this.tradingRecord.exit(
          bar.getEndTime(),
          bar.getClosePrice(),
          getNumFactory().numOf(this.amount)
      );

      // ad bar to positions history
      TradingRecordTestContext.this.tradingRecord.onBar(bar);

      return TradingRecordTestContext.this;
    }


    @Override
    public TradingRecordTestContext at(final double price) {
      TradingRecordTestContext.this.tradingRecord.exit(
          getSimulatedTime(),
          getNumFactory().numOf(price),
          getNumFactory().numOf(this.amount)
      );

      return TradingRecordTestContext.this;
    }
  }


  private Instant getSimulatedTime() {
    return Instant.MIN.plus(this.resolution.getDuration().multipliedBy(this.simulationTime++));
  }
}
