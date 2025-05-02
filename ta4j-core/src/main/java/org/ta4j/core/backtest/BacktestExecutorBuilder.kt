package org.ta4j.core.backtest

import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * @author Lukáš Kvídera
 */
class BacktestExecutorBuilder {
    private var numFactory: NumFactory = DoubleNumFactory

    /** The trading cost models  */
    private var transactionCostModel: CostModel = ZeroCostModel
    private var holdingCostModel: CostModel = ZeroCostModel

    /** The trade execution model to use  */
    private var tradeExecutionModel: TradeExecutionModel = TradeOnCurrentCloseModel()


    fun numFactory(numFactory: NumFactory): BacktestExecutorBuilder {
        this.numFactory = numFactory
        defaultNumFactory = numFactory
        return this
    }


    fun transactionCostModel(transactionCostModel: CostModel): BacktestExecutorBuilder {
        this.transactionCostModel = transactionCostModel
        return this
    }


    fun holdingCostModel(holdingCostModel: CostModel): BacktestExecutorBuilder {
        this.holdingCostModel = holdingCostModel
        return this
    }


    fun tradeExecutionModel(tradeExecutionModel: TradeExecutionModel): BacktestExecutorBuilder {
        this.tradeExecutionModel = tradeExecutionModel
        return this
    }


    fun build(): BacktestExecutor {
        return BacktestExecutor(
            BacktestConfiguration(
                this.numFactory,
                this.transactionCostModel,
                this.holdingCostModel,
                this.tradeExecutionModel
            )
        )
    }
}
