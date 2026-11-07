/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.optimization

import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToLong
import org.ta4j.core.strategy.configuration.Parameter
import org.ta4j.core.strategy.configuration.StrategyConfiguration

class StrategyConfigurationGenerator(parameterDescriptors: List<ParameterDescriptor>) {
    private val parameterDescriptors = parameterDescriptors.associate { it.name to it.range }


    fun generateInitialParameters(): StrategyConfiguration {
        val configuration = StrategyConfiguration()

        for (parameterDescriptor in this.parameterDescriptors.entries) {
            configuration.put(parameterDescriptor.key, generateRandomValue(parameterDescriptor.value))
        }

        return configuration
    }


    fun generateNeighborParameters(current: StrategyConfiguration): StrategyConfiguration {
        val result = StrategyConfiguration()

        for (parameter in current) {
            val modifiedParameter = modifyParameter(parameter)
            result.put(modifiedParameter.name, modifiedParameter.value)
        }

        return result
    }


    val spaceSize: Int
        get() = parameterDescriptors.values
            .map { it.countOfSteps() }
            .reduce { a, b -> a * b }


    private fun modifyParameter(current: Parameter): Parameter {
        val range: ParameterRange = parameterDescriptors[current.name]!!
        val newValue =
            (current.value.toDouble()
                    + (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * range.step)

        // Ensure value stays within allowed range
        val cappedValue = roundToStep(newValue, range.step).coerceIn(range.min, range.max)
        return Parameter(current.name, cappedValue)
    }


    private fun generateRandomValue(range: ParameterRange): Double {
        val steps = range.countOfSteps()
        val randomStep = ThreadLocalRandom.current().nextInt(steps + 1)
        return range.min + (randomStep * range.step)
    }


    private fun roundToStep(value: Double, step: Double): Double {
        return (value / step).roundToLong() * step
    }
}
