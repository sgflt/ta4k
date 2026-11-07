/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

/**
 * Defines when and how trades should be executed during backtesting.
 */
enum class ExecutionMode {
    /**
     * Execute trades immediately at the current bar's close price.
     * This is the default behavior.
     */
    CURRENT_CLOSE,

    /**
     * Execute trades at the next bar's open price.
     * Trades are queued when signals are generated and executed
     * when the next bar arrives.
     */
    NEXT_OPEN
}
