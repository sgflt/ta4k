/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import java.time.Instant
import org.ta4j.core.num.Num

/**
 * Represents a trade order that is waiting to be executed when specific conditions are met.
 *
 * @param type the type of operation (ENTER or EXIT)
 * @param amount the trade amount
 * @param trigger the condition that determines when this order should be executed
 * @param createdAt the timestamp when this order was created
 */
data class PendingOrder(
    val type: OperationType,
    val amount: Num,
    val trigger: ExecutionTrigger,
    val createdAt: Instant,
)
