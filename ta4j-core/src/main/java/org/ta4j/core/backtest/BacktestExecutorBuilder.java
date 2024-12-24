package org.ta4j.core.backtest;

import org.ta4j.core.backtest.analysis.cost.CostModel;
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNumFactory;
import org.ta4j.core.num.NumFactory;

/**
 * @author Lukáš Kvídera
 */
public class BacktestExecutorBuilder {

  private NumFactory numFactory = DoubleNumFactory.getInstance();
  /** The trading cost models */
  private CostModel transactionCostModel = new ZeroCostModel();
  private CostModel holdingCostModel = new ZeroCostModel();

  /** The trade execution model to use */
  private TradeExecutionModel tradeExecutionModel = new TradeOnCurrentCloseModel();


  public BacktestExecutorBuilder numFactory(final NumFactory numFactory) {
    this.numFactory = numFactory;
    return this;
  }


  public BacktestExecutorBuilder transactionCostModel(final CostModel transactionCostModel) {
    this.transactionCostModel = transactionCostModel;
    return this;
  }


  public BacktestExecutorBuilder holdingCostModel(final CostModel holdingCostModel) {
    this.holdingCostModel = holdingCostModel;
    return this;
  }


  public BacktestExecutorBuilder tradeExecutionModel(final TradeExecutionModel tradeExecutionModel) {
    this.tradeExecutionModel = tradeExecutionModel;
    return this;
  }


  public BacktestExecutor build() {
    return new BacktestExecutor(
        new BacktestConfiguration(
            this.numFactory,
            this.transactionCostModel,
            this.holdingCostModel,
            this.tradeExecutionModel
        )
    );
  }
}
