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

    /** The execution mode to use  */
    private var executionMode: ExecutionMode = ExecutionMode.CURRENT_CLOSE


    fun numFactory(numFactory: NumFactory): BacktestExecutorBuilder = apply {
        this.numFactory = numFactory
        defaultNumFactory = numFactory
    }


    fun transactionCostModel(transactionCostModel: CostModel): BacktestExecutorBuilder = apply {
        this.transactionCostModel = transactionCostModel
    }


    fun holdingCostModel(holdingCostModel: CostModel): BacktestExecutorBuilder = apply {
        this.holdingCostModel = holdingCostModel
    }


    fun executionMode(executionMode: ExecutionMode): BacktestExecutorBuilder = apply {
        this.executionMode = executionMode
    }


    fun build(): BacktestExecutor = BacktestExecutor(
        BacktestConfiguration(
            this.numFactory,
            this.transactionCostModel,
            this.holdingCostModel,
            this.executionMode
        )
    )
}
