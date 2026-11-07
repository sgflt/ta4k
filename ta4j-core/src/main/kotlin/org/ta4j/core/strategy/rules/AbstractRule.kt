/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.ta4j.core.strategy.Rule

/**
 * An abstract trading [rule][Rule].
 */
abstract class AbstractRule : Rule {
    protected val log: Logger = LoggerFactory.getLogger(javaClass)


    /**
     * Traces the `isSatisfied()` method calls.
     *
     * @param isSatisfied true if the rule is satisfied, false otherwise
     */
    protected fun traceIsSatisfied(isSatisfied: Boolean) {
        if (this.log.isTraceEnabled) {
            // TODO
            this.log.trace("{}", this)
        }
    }
}
