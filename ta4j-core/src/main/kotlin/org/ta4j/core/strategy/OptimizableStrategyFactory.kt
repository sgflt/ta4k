/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy

import org.ta4j.core.strategy.optimization.ParameterDescriptor

/**
 * Use this interface if you don't know how to set up parameters of chosen strategy.
 *
 * This factory provides description of parameters that may be fine-tuned by optimization algorithm.
 */
interface OptimizableStrategyFactory<T : Strategy> : StrategyFactory<T> {
    /**
     * @return all parameters that may be fine-tuned
     */
    val parameterDescriptors: List<ParameterDescriptor>
}
